#!/usr/bin/env python3
"""
Build HerSync launcher assets from scripts/icon_source.png.

Uses the full reference image (no fixed center crop): chroma-keys the outer
background, trims empty margins and the label row under the squircle, then
fits the entire icon into a square with even padding and scales — artwork is
not clipped.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "scripts" / "icon_source.png"
RES = ROOT / "app" / "src" / "main" / "res"

# Chroma-key screenshot background (charcoal) to transparent
BG_KEY = (31, 31, 30)
BG_THRESH = 42

# Launcher adaptive background (pale pink — matches squircle base)
PINK_BG = (253, 226, 233, 255)  # #FDE2E9

DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

FG_SIZE = 432

# Row is treated as “icon body” vs sparse label / gap below
ROW_OPAQUE_MIN = 0.52


def chroma_key_transparent(im: Image.Image) -> Image.Image:
    im = im.convert("RGBA")
    px = im.load()
    w, h = im.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            d = abs(r - BG_KEY[0]) + abs(g - BG_KEY[1]) + abs(b - BG_KEY[2])
            if d < BG_THRESH or (r < 48 and g < 48 and b < 48 and d < 80):
                px[x, y] = (r, g, b, 0)
    return im


def opaque_row_fraction(im: Image.Image, y: int) -> float:
    px = im.load()
    w = im.width
    return sum(1 for x in range(w) if px[x, y][3] > 30) / max(w, 1)


def vertical_trim_icon_body(keyed: Image.Image) -> Image.Image:
    """Remove top/bottom margins and the “HerSync” band under the squircle."""
    w, h = keyed.size
    y_top = 0
    for y in range(h):
        if opaque_row_fraction(keyed, y) >= ROW_OPAQUE_MIN:
            y_top = y
            break
    y_bottom = h - 1
    for y in range(h - 1, -1, -1):
        if opaque_row_fraction(keyed, y) >= ROW_OPAQUE_MIN:
            y_bottom = y
            break
    if y_bottom < y_top:
        return keyed
    return keyed.crop((0, y_top, w, y_bottom + 1))


def alpha_bbox(im: Image.Image, alpha_min: int = 28) -> tuple[int, int, int, int]:
    px = im.load()
    w, h = im.size
    min_x, min_y = w, h
    max_x, max_y = 0, 0
    for y in range(h):
        for x in range(w):
            if px[x, y][3] > alpha_min:
                min_x = min(min_x, x)
                min_y = min(min_y, y)
                max_x = max(max_x, x)
                max_y = max(max_y, y)
    if max_x < min_x:
        return 0, 0, w - 1, h - 1
    return min_x, min_y, max_x, max_y


def fit_in_square_padded(im: Image.Image, pad_px: int) -> Image.Image:
    """Center artwork in a square canvas; no clipping, only transparent padding."""
    min_x, min_y, max_x, max_y = alpha_bbox(im)
    core = im.crop((min_x, min_y, max_x + 1, max_y + 1))
    cw, ch = core.size
    side = max(cw, ch) + 2 * pad_px
    out = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    ox = (side - cw) // 2
    oy = (side - ch) // 2
    out.paste(core, (ox, oy), core)
    return out


def composite_on_pink(fg_rgba: Image.Image, size: int) -> Image.Image:
    fg = fg_rgba.resize((size, size), Image.Resampling.LANCZOS)
    base = Image.new("RGBA", (size, size), PINK_BG)
    return Image.alpha_composite(base, fg)


def main() -> None:
    if not SOURCE.exists():
        raise SystemExit(f"Missing {SOURCE} — add the reference as icon_source.png")

    keyed = chroma_key_transparent(Image.open(SOURCE))
    trimmed = vertical_trim_icon_body(keyed)

    pad = max(12, min(trimmed.width, trimmed.height) // 40)
    squared = fit_in_square_padded(trimmed, pad_px=pad)

    fg = squared.resize((FG_SIZE, FG_SIZE), Image.Resampling.LANCZOS)
    fg_path = RES / "drawable" / "ic_launcher_foreground.png"
    fg_path.parent.mkdir(parents=True, exist_ok=True)
    fg.save(fg_path, "PNG")
    print(f"Wrote {fg_path} (full icon, letterboxed square, {FG_SIZE}px)")

    for folder, dim in DENSITIES.items():
        out_dir = RES / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        flat = composite_on_pink(squared, dim)
        flat.save(out_dir / "ic_launcher.png", "PNG")
        flat.save(out_dir / "ic_launcher_round.png", "PNG")
        print(f"Wrote {folder} ({dim}x{dim})")


if __name__ == "__main__":
    main()
