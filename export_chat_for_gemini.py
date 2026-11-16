import json
import re
from pathlib import Path

transcript_path = Path(r"C:\Users\sahaa\.gemini\antigravity\brain\60cd456b-a852-4fdd-8962-2f7597c08c46\.system_generated\logs\transcript.jsonl")
output_md = Path(r"C:\Users\sahaa\.gemini\antigravity\scratch\SpiralNeuralNetworkEngine\CHAT_TRANSCRIPT_FOR_GEMINI.md")
output_txt = Path(r"C:\Users\sahaa\.gemini\antigravity\scratch\SpiralNeuralNetworkEngine\CHAT_TRANSCRIPT_FOR_GEMINI.txt")

entries = []
with open(transcript_path, "r", encoding="utf-8") as f:
    for line in f:
        try:
            data = json.loads(line)
            source = data.get("source")
            stype = data.get("type")
            content = data.get("content", "")
            
            if source == "USER_EXPLICIT" and stype == "USER_INPUT":
                # Clean user input
                clean_text = re.sub(r"<USER_REQUEST>\s*", "", content)
                clean_text = re.sub(r"\s*</USER_REQUEST>", "", clean_text).strip()
                if clean_text:
                    entries.append({"role": "User", "text": clean_text})
            elif source == "MODEL" and stype == "PLANNER_RESPONSE":
                if content and len(content.strip()) > 10:
                    # Ignore brief progress notices if needed or keep full responses
                    entries.append({"role": "Antigravity (AI Assistant)", "text": content.strip()})
        except Exception:
            continue

# Group / format dialogue
doc = []
doc.append("# Spiral Neural Network Interface Engine — Complete Chat & Project Transcript")
doc.append("")
doc.append("> **Context for Google Gemini:**")
doc.append("> This document contains the complete conversation, requirements, mathematical derivations, codebase breakdown, and project report for the **Spiral Neural Network Interface Engine Using Java**.")
doc.append("> **Institution:** Jaypee Institute of Information Technology (JIIT), Noida")
doc.append("> **Course:** B.Tech III-Semester CSE, OOPS Using Java Lab (Course Code: 24B15CS215)")
doc.append("> **Submitted To:** Dr. K.Rajalakshmi")
doc.append("> **Authors:** Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)")
doc.append("> **Repository:** https://github.com/BlulightningXD/SpiralNeuralNetworkEngine")
doc.append("")
doc.append("---")
doc.append("")

turn_count = 1
for entry in entries:
    role = entry["role"]
    text = entry["text"]
    if role == "User":
        doc.append(f"## 👤 User Prompt #{turn_count}")
        doc.append(f"> **{text}**")
        doc.append("")
        turn_count += 1
    else:
        doc.append(f"### 🤖 Assistant Response")
        doc.append(text)
        doc.append("")
        doc.append("---")
        doc.append("")

full_content = "\n".join(doc)

with open(output_md, "w", encoding="utf-8") as f:
    f.write(full_content)

with open(output_txt, "w", encoding="utf-8") as f:
    f.write(full_content)

# Also save copy in artifact dir
artifact_copy = Path(r"C:\Users\sahaa\.gemini\antigravity\brain\60cd456b-a852-4fdd-8962-2f7597c08c46\CHAT_TRANSCRIPT_FOR_GEMINI.md")
with open(artifact_copy, "w", encoding="utf-8") as f:
    f.write(full_content)

print(f"Successfully generated transcript files:")
print(f"  Markdown: {output_md} ({len(full_content)} chars)")
print(f"  Text: {output_txt}")
