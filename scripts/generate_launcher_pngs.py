#!/usr/bin/env python3
"""Generate legacy mipmap PNGs (pre–API 26) for HerSync launcher icon."""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

RES = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"

CHARCOAL = (26, 26, 28, 255)
PINK = (255, 105, 180, 255)
WHITE = (245, 245, 245, 255)
RING = (255, 255, 255, 102)  # ~40% white

DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def draw_icon(size: int) -> Image.Image:
    img = Image.new("RGBA", (size, size), CHARCOAL)
    d = ImageDraw.Draw(img)
    cx, cy = size / 2, size * 0.52
    r_main = size * 0.26
    r_hl = size * 0.10
    hl_cy = size * 0.35
    d.ellipse(
        [cx - r_main, cy - r_main, cx + r_main, cy + r_main],
        fill=PINK,
    )
    d.ellipse(
        [cx - r_hl, hl_cy - r_hl, cx + r_hl, hl_cy + r_hl],
        fill=WHITE,
    )
    r_ring = size * 0.22
    stroke = max(1, size // 54)
    d.ellipse(
        [cx - r_ring, cy - r_ring, cx + r_ring, cy + r_ring],
        outline=RING[:3] + (RING[3],),
        width=stroke,
    )
    return img


def main() -> None:
    for folder, dim in DENSITIES.items():
        out_dir = RES / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        im = draw_icon(dim)
        im.save(out_dir / "ic_launcher.png", "PNG")
        im.save(out_dir / "ic_launcher_round.png", "PNG")
        print(f"Wrote {folder} ({dim}x{dim})")


if __name__ == "__main__":
    main()
