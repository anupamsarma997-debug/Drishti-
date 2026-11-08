# Third-party data and software notices

## OBDex generic OBD-II reference data

- Project: https://github.com/foerbsnavi/OBDex
- Data endpoint: https://foerbsnavi.github.io/OBDex/generic.min.json
- OBDex declares its data files under CC0 1.0 Universal: https://github.com/foerbsnavi/OBDex/blob/main/LICENSE-DATA
- OBDex declares its tooling separately under MIT: https://github.com/foerbsnavi/OBDex/blob/main/LICENSE-CODE

OBDex's maintainer states that DTC identifiers and English titles in its bulk-import waves derive from Wal33D/dtc-database under MIT, while the remaining descriptions and cause/repair prose are independently authored and declared under OBDex's CC0 data terms: https://github.com/foerbsnavi/OBDex/issues/8

The MIT notice for the identified upstream source is reproduced below. Vidya's downloader imports generic DTC reference fields only; it does not import OBDex repair-cost estimates or repair procedures. Per-record source URLs are retained when present. The downloaded content is not OEM-verified and does not establish exact vehicle compatibility.

## MIT License — Wal33D/dtc-database

Copyright (c) 2024 Wal33D (Waleed Judah)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
