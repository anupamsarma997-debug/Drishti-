import tempfile
import unittest
import zipfile
from pathlib import Path

from audit_dataset_archive import inspect_zip


class ArchiveAuditTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.archive = Path(self.temp.name) / "dataset.zip"

    def tearDown(self):
        self.temp.cleanup()

    def test_inventory_and_license_candidate(self):
        with zipfile.ZipFile(self.archive, "w") as z:
            z.writestr("images/part-001.jpg", b"fake-jpeg")
            z.writestr("LICENSE.txt", "CC BY 4.0")
            z.writestr("labels/annotations.json", "{}")
        report = inspect_zip(self.archive, 100, 10000)
        self.assertEqual(3, report["file_count"])
        self.assertEqual(1, report["image_count_by_extension"])
        self.assertIn("LICENSE.txt", report["license_notice_candidates"])
        self.assertEqual("REVIEW_REQUIRED", report["license_status"])

    def test_path_traversal_is_reported(self):
        with zipfile.ZipFile(self.archive, "w") as z:
            z.writestr("../escape.txt", "bad")
        report = inspect_zip(self.archive, 100, 10000)
        self.assertEqual(["../escape.txt"], report["unsafe_member_paths"])

    def test_member_limit(self):
        with zipfile.ZipFile(self.archive, "w") as z:
            z.writestr("one.txt", "1")
            z.writestr("two.txt", "2")
        with self.assertRaises(ValueError):
            inspect_zip(self.archive, 1, 10000)

    def test_uncompressed_size_limit(self):
        with zipfile.ZipFile(self.archive, "w") as z:
            z.writestr("large.txt", "123456789")
        with self.assertRaises(ValueError):
            inspect_zip(self.archive, 10, 4)


if __name__ == "__main__":
    unittest.main()
