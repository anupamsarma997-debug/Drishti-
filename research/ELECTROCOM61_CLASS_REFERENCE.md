# ElectroCom61 class-name cross-reference (not a verified class-ID map)

## Purpose

This file records the 61 class names shown by a public Roboflow Universe page that presents an ElectroCom-61 dataset mirror. It is a secondary cross-reference for checking an extracted ElectroCom61 v2 archive; it is **not** proof of the original Mendeley class-index order and must not be used to map YOLO class IDs to names.

## Primary dataset facts

- Primary dataset: [ElectroCom61 v2 on Mendeley Data](https://data.mendeley.com/datasets/6scy6h8sjz/2)
- Data article: [ElectroCom61, Data in Brief (2025)](https://pmc.ncbi.nlm.nih.gov/articles/PMC11847280/)
- DOI: 10.17632/6scy6h8sjz.2
- The article reports 2,121 images, 61 electronic-component classes, and 12,937 annotations. It describes a CSV metadata file containing image name, object names, background, capture device and split.
- Declared dataset licence: CC BY 4.0. Preserve attribution and inspect archive notices before reuse.

## Secondary class-name list

Source: [Roboflow Universe ElectroCom-61 page](https://universe.roboflow.com/datasetsynthesis/electrocom-61), consulted 2026-11-08. The names below are transcribed as a discovery aid; capitalization and hyphenation may follow the mirror's display conventions.

1. Resistor
2. Diode
3. Inductor
4. 1-5-Volt-Battery
5. 3-3-Volt-Battery
6. 7-Segment-Display
7. 9-Volt-Battery
8. Arduino-Mega
9. Arduino-Nano
10. Arduino-Uno
11. BJT-Transistor
12. Bluetooth-Module
13. Breadboard
14. Bridge-Rectifier
15. Buck-Converter
16. Buzzer
17. Capacitor-10mf
18. Capacitor-470mf
19. DC-Motor
20. ESP32
21. ESP32-CAM
22. Film-Capacitor
23. FT-232-USB-Serial-Module
24. Fuse
25. Fuse-Base
26. Gas-Sensor
27. GSM-Module
28. Heat-Sink
29. High-Voltage-Ceramic-Capacitor
30. Humidity-Sensor
31. IC-Base-14-Pin
32. IC-Base-28-Pin
33. IC-Chip
34. IGBT
35. IR-Sensor
36. Keypad
37. LCD-Display
38. LDR-Sensor
39. LED-Light
40. Low-Voltage-Ceramic-Capacitor
41. MLC-Capacitor
42. MOSFET
43. Motion-Sensor
44. Motor-Driver
45. NTC-Thermistor
46. OLED-Display
47. Pin-Header
48. Push-Switch
49. Raindrops-Module
50. Relay-Module
51. RFID-Scanner
52. Rocker-Switch
53. Servo-Motor
54. Soil-Moisture-Sensor
55. Sonar-Sensor
56. Tact-Switch
57. Taper-Potentiometer
58. TCRT5000
59. Trimmer-Potentiometer
60. Water-Sensor
61. Zener-Diode

The secondary listing includes Relay-Module, bringing the cross-reference to 61 names. This remains a secondary class-name reference only: it does not establish the primary archive's exact YOLO class-ID order. Extract and verify the definitive mapping from the primary v2 archive's `data.yaml` and metadata before training.

## Training gate

Before any model training or app integration:
1. Obtain the primary v2 archive and preserve its original files and licence/attribution notices.
2. Inspect the included data configuration and metadata CSV.
3. Extract the exact class-ID-to-name mapping from the archive itself, not this secondary list.
4. Verify every label against sampled image pixels and metadata; record unknown or ambiguous labels.
5. Run `python research/tools/audit_electrocom61.py --root /path/to/ElectroCom61`.
6. Review duplicates, capture-device/background leakage, class imbalance and split integrity.
7. Keep this model limited to electronic-component object detection; it does not diagnose faults or identify vehicle/farm-machine parts.

## Current Vidya status

No ElectroCom61 archive has been downloaded, imported, semantically verified or used for training. No ElectroCom61-trained model or accuracy result is claimed.
