import tempfile
import unittest
from pathlib import Path

import pyarrow as pa
import pyarrow.parquet as pq
from PIL import Image
from io import BytesIO

from prepare_i3d_yolo_dataset import CLASS_NAMES, main, parse_boxes, split_for_hash
from unittest.mock import patch


class I3DPreparationTest(unittest.TestCase):
    def test_class_map_has_16_ordered_names(self):
        self.assertEqual(16, len(CLASS_NAMES))
        self.assertEqual("ball_bearing", CLASS_NAMES[0])
        self.assertEqual("wrench", CLASS_NAMES[15])

    def test_multi_box_annotation(self):
        boxes = parse_boxes("0 0.5 0.5 0.2 0.3 15 0.2 0.2 0.1 0.1")
        self.assertEqual(2, len(boxes))
        self.assertEqual(15, boxes[1][0])

    def test_bad_class_or_coordinates_rejected(self):
        with self.assertRaises(ValueError):
            parse_boxes("16 0.5 0.5 0.2 0.2")
        with self.assertRaises(ValueError):
            parse_boxes("1 1.2 0.5 0.2 0.2")


    def test_duplicates_across_parquet_shards_are_removed_before_split(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            buffer = BytesIO()
            Image.new("RGB", (24, 24), color="purple").save(buffer, format="PNG")
            row = {"image": {"bytes": buffer.getvalue(), "path": "same.png"},
                   "yolo_annotation": "1 0.5 0.5 0.3 0.3"}
            first, second = root / "first.parquet", root / "second.parquet"
            pq.write_table(pa.Table.from_pylist([row]), first)
            pq.write_table(pa.Table.from_pylist([row]), second)
            output = root / "prepared"
            with patch("sys.argv", ["prepare", "--parquet", str(first), str(second), "--output", str(output)]):
                main()
            report = __import__("json").loads((output / "preparation_report.json").read_text())
            self.assertEqual(2, report["source_rows"])
            self.assertEqual(1, report["unique_primary_images"])
            self.assertEqual(1, report["duplicate_primary_rows_removed"])
            self.assertEqual(1, sum(report["split_images"].values()))
            split_boxes = report["split_class_box_counts"]
            self.assertEqual(1, sum(sum(counts.values()) for counts in split_boxes.values()))
            self.assertEqual(report["class_box_counts"]["gear"], 1)

    def test_conflicting_labels_for_same_image_are_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            buffer = BytesIO()
            Image.new("RGB", (24, 24), color="orange").save(buffer, format="PNG")
            raw = buffer.getvalue()
            rows = [
                {"image": {"bytes": raw, "path": "same.png"}, "yolo_annotation": "1 0.5 0.5 0.3 0.3"},
                {"image": {"bytes": raw, "path": "same.png"}, "yolo_annotation": "2 0.5 0.5 0.3 0.3"},
            ]
            parquet = root / "conflict.parquet"
            pq.write_table(pa.Table.from_pylist(rows), parquet)
            with patch("sys.argv", ["prepare", "--parquet", str(parquet), "--output", str(root / "out")]):
                with self.assertRaises(SystemExit):
                    main()

    def test_split_is_deterministic(self):
        self.assertEqual(split_for_hash("abc123", 7), split_for_hash("abc123", 7))
        self.assertIn(split_for_hash("abc123", 7), {"train", "val", "test"})

    def test_parquet_to_yolo_deduplicates_primary_images(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            rows = []
            for color in ("red", "green", "blue", "yellow"):
                buffer = BytesIO()
                Image.new("RGB", (24, 24), color=color).save(buffer, format="PNG")
                raw = buffer.getvalue()
                rows.append({"image": {"bytes": raw, "path": color + ".png"},
                             "yolo_annotation": "2 0.5 0.5 0.4 0.4"})
            rows.append(dict(rows[0]))
            parquet = root / "sample.parquet"
            pq.write_table(pa.Table.from_pylist(rows), parquet)
            output = root / "prepared"
            with patch("sys.argv", ["prepare", "--parquet", str(parquet), "--output", str(output)]):
                main()
            report = __import__("json").loads((output / "preparation_report.json").read_text())
            self.assertEqual(5, report["source_rows"])
            self.assertEqual(4, report["unique_primary_images"])
            self.assertEqual(1, report["duplicate_primary_rows_removed"])
            self.assertEqual(4, report["valid_yolo_boxes"])
            self.assertFalse(report["conditioning_images_exported"])
            image_files = list((output / "images").rglob("*.jpg"))
            label_files = list((output / "labels").rglob("*.txt"))
            self.assertEqual(4, len(image_files))
            self.assertEqual(4, len(label_files))
            self.assertTrue((output / "dataset.yaml").is_file())
            split_boxes = report["split_class_box_counts"]
            self.assertEqual(4, sum(sum(counts.values()) for counts in split_boxes.values()))
            self.assertEqual(4, sum(report["class_box_counts"].values()))


if __name__ == "__main__":
    unittest.main()
