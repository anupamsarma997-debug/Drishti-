import tempfile
import unittest
from pathlib import Path

from audit_electrocom61 import audit


class ElectroCom61AuditTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        for split in ("Train", "Valid", "Test"):
            (self.root / split / "Image").mkdir(parents=True)
            (self.root / split / "Label").mkdir(parents=True)
        for split in ("Train", "Valid", "Test"):
            (self.root / split / "Image" / "sample.jpg").write_bytes(b"image-" + split.encode())
            (self.root / split / "Label" / "sample.txt").write_text("0 0.5 0.5 0.25 0.25\n", encoding="utf-8")

    def tearDown(self):
        self.temp.cleanup()

    def test_valid_yolo_archive(self):
        errors, report = audit(self.root)
        self.assertEqual([], errors)
        self.assertEqual(3, report["images"])
        self.assertEqual(3, report["bounding_boxes"])
        self.assertEqual(1, report["observed_class_ids"])

    def test_invalid_class_id(self):
        (self.root / "Train" / "Label" / "sample.txt").write_text("61 0.5 0.5 0.2 0.2\n", encoding="utf-8")
        errors, _ = audit(self.root)
        self.assertTrue(any("class_id" in error for error in errors))

    def test_invalid_box_coordinates(self):
        (self.root / "Valid" / "Label" / "sample.txt").write_text("2 1.2 0.5 0.2 0.2\n", encoding="utf-8")
        errors, _ = audit(self.root)
        self.assertTrue(any("within [0, 1]" in error for error in errors))

    def test_missing_and_orphan_label_pairs(self):
        (self.root / "Train" / "Label" / "sample.txt").unlink()
        (self.root / "Test" / "Label" / "orphan.txt").write_text("1 0.5 0.5 0.2 0.2\n", encoding="utf-8")
        errors, _ = audit(self.root)
        self.assertTrue(any("no matching label file" in error for error in errors))
        self.assertTrue(any("no matching image" in error for error in errors))

    def test_duplicate_image_bytes_across_splits(self):
        (self.root / "Valid" / "Image" / "sample.jpg").write_bytes(b"image-Train")
        errors, _ = audit(self.root)
        self.assertTrue(any("cross splits" in error for error in errors))

    def test_missing_split_fails(self):
        import shutil
        shutil.rmtree(self.root / "Test")
        errors, _ = audit(self.root)
        self.assertTrue(any("missing split directory" in error for error in errors))

    def test_duplicate_image_stems_are_rejected(self):
        duplicate = self.root / "Train" / "Image" / "nested" / "sample.png"
        duplicate.parent.mkdir()
        duplicate.write_bytes(b"different image")
        errors, _ = audit(self.root)
        self.assertTrue(any("duplicate image filename stem" in error for error in errors))

    def test_duplicate_label_stems_are_rejected(self):
        duplicate = self.root / "Valid" / "Label" / "nested" / "sample.txt"
        duplicate.parent.mkdir()
        duplicate.write_text("1 0.5 0.5 0.2 0.2\n", encoding="utf-8")
        errors, _ = audit(self.root)
        self.assertTrue(any("duplicate label filename stem" in error for error in errors))

    def test_metadata_csv_matches_image_names_and_splits(self):
        metadata = self.root / "Metadata_ElectroCom61.csv"
        metadata.write_text(
            "IMAGE_NAME,TYPE_OF_OBJECTS,OBJECT_NAME,BACKGROUND,DEVICE_NAME,DATA_TYPE\\n"
            "sample.jpg,1,1_Resistor,white,Phone,Train\\n"
            "sample.jpg,1,1_Resistor,white,Phone,Valid\\n"
            "sample.jpg,1,1_Resistor,white,Phone,Test\\n",
            encoding="utf-8",
        )
        errors, report = audit(self.root)
        self.assertEqual([], errors)
        self.assertEqual("checked", report["metadata_csv"]["status"])
        self.assertEqual(3, report["metadata_csv"]["rows"])

    def test_metadata_split_mismatch_is_rejected(self):
        metadata = self.root / "Metadata_ElectroCom61.csv"
        metadata.write_text(
            "IMAGE_NAME,TYPE_OF_OBJECTS,OBJECT_NAME,BACKGROUND,DEVICE_NAME,DATA_TYPE\\n"
            "sample.jpg,1,1_Resistor,white,Phone,Test\\n",
            encoding="utf-8",
        )
        errors, _ = audit(self.root)
        self.assertTrue(any("DATA_TYPE says" in error for error in errors))
        self.assertTrue(any("row count" in error for error in errors))

    def test_empty_label_file_is_valid_negative_sample(self):
        (self.root / "Train" / "Label" / "sample.txt").write_text("", encoding="utf-8")
        errors, report = audit(self.root)
        self.assertEqual([], errors)
        self.assertEqual(2, report["bounding_boxes"])


if __name__ == "__main__":
    unittest.main()
