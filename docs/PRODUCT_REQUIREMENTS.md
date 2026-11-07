# Drishti AI — Product Requirements

## 1. Product vision
Drishti AI is a camera-and-voice assistant for safer understanding of technical work. A user points an Android phone at equipment, describes the task, and receives cautious, understandable guidance grounded in the relevant manufacturer documentation.

Tagline: **See. Understand. Prevent.**

The tagline is aspirational; it is not a guarantee that the app prevents accidents.

## 2. Initial target users
- Learners who need help understanding equipment manuals.
- Home users performing low-risk inspection and maintenance.
- Motorcycle/scooter owners checking visible, non-invasive conditions.
- Technicians who want faster access to model-specific documentation.

The first release must not target unsupervised hazardous electrical or industrial repair.

## 3. Initial supported categories
Start with controlled, non-invasive guidance for:
- Motorcycles and scooters: external visual checks, tyre condition prompts, lights, mirrors, visible leaks, and maintenance-manual lookup.
- Household appliances: model identification, manual lookup, external condition checks, and safe user-level maintenance.
- Pumps, motors, and generators: external visual inspection only, with shutdown/isolation warnings and professional escalation.
- Electrical equipment: hazard recognition and safe escalation only; no live wiring diagnosis or repair instructions.

## 4. Core user journey
1. User selects a language and accepts a plain-language safety/privacy explanation.
2. User selects equipment category or scans a label/QR/model number.
3. App asks what the user is trying to do.
4. Camera preview guides the user to capture a clear image; user controls capture and upload.
5. App reports visible observations separately from uncertain hypotheses.
6. App identifies relevant manual sections and cites the document/page.
7. App presents one safe step at a time, with a clear stop/escalate option.
8. User can ask a follow-up by voice or text.
9. App records a local session summary only with user consent.

## 5. Functional requirements
- CameraX preview and still-image capture.
- Equipment/model identification with manual correction by the user.
- PDF/manual import and searchable document library.
- Retrieval grounded in the selected equipment's documentation.
- Page-level references for manual-derived procedures.
- Voice input and text-to-speech output.
- Initial languages: Assamese, Hindi, English, Bengali; expand after quality testing.
- Clear confidence/uncertainty communication.
- Visual annotations: labels, arrows, numbered steps; ARCore optional, with a non-AR overlay fallback.
- Offline access to previously downloaded manuals and safety notices.
- User-controlled deletion of captured images and session history.

## 6. Non-functional requirements
- Work on supported low- and mid-range Android devices.
- Graceful operation on slow or absent networks.
- Minimize image retention and data collection.
- Encrypt data in transit and protect stored user documents.
- Accessible controls, readable text, and TalkBack support.
- Avoid blocking the camera preview while inference runs.

## 7. Explicit exclusions for early versions
- No promise to predict every failure or prevent all accidents.
- No determination that electrical equipment is de-energized from an image.
- No instructions for live electrical work, high-voltage repair, or bypassing safety systems.
- No autonomous control of machinery, vehicles, tools, or electrical systems.
- No medical, emergency-response, or legally certified inspection claims.

## 8. MVP acceptance criteria
- App clearly explains limitations before first camera use.
- User can grant/revoke camera and microphone permissions.
- User can capture an image and receive a cautious description of visible features.
- The system refuses hazardous procedural requests and provides a safe escalation message.
- Manual-based answers include the correct document and page reference.
- Low-confidence or unsupported equipment results in a clear stop/ask-a-professional state.
- Offline and network-failure states are tested.
- No secrets or personal data are present in the repository.

## 9. Success measures
Measure usability and reliability in controlled evaluations: correct manual retrieval, citation accuracy, safe refusal rate, false reassurance rate, latency on target devices, accessibility, and user comprehension. Do not use unvalidated synthetic scores as real-world safety results.
