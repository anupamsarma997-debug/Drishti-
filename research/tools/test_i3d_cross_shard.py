import json
import tempfile
import unittest
from io import BytesIO
from pathlib import Path

import pyarrow as pa
import pyarrow.parquet as pq
from PIL import Image

from audit_i3d_cross_shard import main
from unittest.mock import patch


class CrossShardAuditTest(unittest.TestCase):
    def make_row(self, color, annotation):
        buffer = BytesIO()
        Image.new("RGB", (16, 16), color=color).save(buffer, format="PNG")
        return {"image": {"bytes": buffer.getvalue(), "path": color + ".png"},
                "yolo_annotation": annotation}

    def test_duplicate_primary_images_are_detected_across_shards(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            same = self.make_row("red", "1 0.5 0.5 0.2 0.2")
            first, second = root / "a.parquet", root / "b.parquet"
            pq.write_table(pa.Table.from_pylist([same]), first)
            pq.write_table(pa.Table.from_pylist([same]), second)
            output = root / "report.json"
            with patch("sys.argv", ["audit", "--parquet", str(first), str(second), "--output", str(output)]):
                with self.assertRaises(SystemExit) as error:
                    main()
            self.assertEqual(1, error.exception.code)
            report = json.loads(output.read_text())
            self.assertEqual(1, report["globally_unique_primary_images"])
            self.assertEqual(1, report["cross_shard_and_within_input_duplicate_rows"])

    def test_conflicting_duplicate_annotations_are_flagged(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            buffer = BytesIO()
            Image.new("RGB", (16, 16), color="blue").save(buffer, format="PNG")
            raw = buffer.getvalue()
            first = {"image": {"bytes": raw, "path": "same.png"},
                     "yolo_annotation": "1 0.5 0.5 0.2 0.2"}
            second = {"image": {"bytes": raw, "path": "same.png"},
                      "yolo_annotation": "2 0.5 0.5 0.2 0.2"}
            a, b = root / "a.parquet", root / "b.parquet"
            pq.write_table(pa.Table.from_pylist([first]), a)
            pq.write_table(pa.Table.from_pylist([second]), b)
            output = root / "report.json"
            with patch("sys.argv", ["audit", "--parquet", str(a), str(b), "--output", str(output)]):
                with self.assertRaises(SystemExit) as error:
                    main()
            self.assertEqual(1, error.exception.code)
            report = json.loads(output.read_text())
            self.assertEqual(1, report["conflicting_duplicate_annotations"])

    def test_blank_annotations_are_allowed_as_empty_labels(self):
        from audit_i3d_cross_shard import normalized_label
        self.assertEqual((), normalized_label(""))
        self.assertEqual((), normalized_label(None))


if __name__ == "__main__":
    unittest.main()
