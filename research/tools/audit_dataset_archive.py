#!/usr/bin/env python3
"""Inventory a ZIP dataset archive without extracting it or trusting its labels.

This is a first-pass archive audit, not a licence opinion or annotation validator.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import zipfile
from collections import Counter
from pathlib import PurePosixPath
from pathlib import Path

IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif", ".tif", ".tiff"}
LICENSE_NAMES = {"license", "licence", "copying", "copyright", "notice", "third_party_notices"}
MAX_MEMBERS_DEFAULT = 250_000
MAX_UNCOMPRESSED_DEFAULT = 20 * 1024 * 1024 * 1024


def file_sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def inspect_zip(path: Path, max_members: int, max_uncompressed: int) -> dict:
    with zipfile.ZipFile(path) as archive:
        infos = [info for info in archive.infolist() if not info.is_dir()]
        if len(infos) > max_members:
            raise ValueError(f"archive has {len(infos)} files; limit is {max_members}")
        total_size = sum(info.file_size for info in infos)
        if total_size > max_uncompressed:
            raise ValueError(f"uncompressed size {total_size} exceeds limit {max_uncompressed}")

        unsafe_paths = []
        image_files = []
        license_files = []
        extensions = Counter()
        zero_byte_files = []
        for info in infos:
            # ZIP paths use forward slashes. Reject absolute paths and traversal.
            member = PurePosixPath(info.filename.replace("\\", "/"))
            if member.is_absolute() or ".." in member.parts or re.match(r"^[A-Za-z]:", info.filename):
                unsafe_paths.append(info.filename)
            suffix = member.suffix.lower()
            extensions[suffix or "<no-extension>"] += 1
            if suffix in IMAGE_EXTENSIONS:
                image_files.append(info.filename)
            stem = member.stem.lower().replace("-", "_").replace(" ", "_")
            if stem in LICENSE_NAMES or stem.startswith(("license_", "licence_", "copying_")):
                license_files.append(info.filename)
            if info.file_size == 0:
                zero_byte_files.append(info.filename)

        return {
            "archive": str(path),
            "archive_sha256": file_sha256(path),
            "archive_bytes": path.stat().st_size,
            "file_count": len(infos),
            "uncompressed_bytes": total_size,
            "image_count_by_extension": len(image_files),
            "extension_counts": dict(sorted(extensions.items())),
            "license_notice_candidates": license_files,
            "unsafe_member_paths": unsafe_paths,
            "zero_byte_files": zero_byte_files,
            "image_member_examples": image_files[:20],
            "license_status": "REVIEW_REQUIRED",
            "annotation_status": "NOT_AUDITED",
            "training_status": "NOT_TRAINED",
        }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive", type=Path)
    parser.add_argument("--max-members", type=int, default=MAX_MEMBERS_DEFAULT)
    parser.add_argument("--max-uncompressed-bytes", type=int, default=MAX_UNCOMPRESSED_DEFAULT)
    args = parser.parse_args()
    if not args.archive.is_file():
        parser.error(f"archive does not exist: {args.archive}")
    try:
        report = inspect_zip(args.archive, args.max_members, args.max_uncompressed_bytes)
    except (zipfile.BadZipFile, OSError, ValueError) as exc:
        parser.exit(2, f"Archive audit failed: {exc}\n")
    print(json.dumps(report, indent=2, sort_keys=True))
    if report["unsafe_member_paths"]:
        return 1
    print("\nInventory complete. Review the licence and annotations manually; no files were extracted.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
