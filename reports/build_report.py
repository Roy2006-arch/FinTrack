from pathlib import Path
from io import BytesIO
from PIL import Image, ImageDraw, ImageFont
from docx import Document
from docx.shared import Inches, Pt, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK, WD_LINE_SPACING
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.enum.section import WD_SECTION
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.enum.style import WD_STYLE_TYPE

ROOT = Path(__file__).resolve().parents[1]
REPORTS = ROOT / "reports"
ASSETS = REPORTS / "assets"
OUT = REPORTS / "FinTrack_Project_Report.docx"

FONT = "Times New Roman"
BODY = Pt(12)
CAPTION = Pt(10)


def font_for(run, size=BODY, bold=False, italic=False):
    run.font.name = FONT
    run._element.rPr.rFonts.set(qn("w:eastAsia"), FONT)
    run.font.size = size
    run.font.bold = bold
    run.font.italic = italic


def set_cell_shading(cell, fill):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = tc_pr.find(qn("w:shd"))
    if shd is None:
        shd = OxmlElement("w:shd")
        tc_pr.append(shd)
    shd.set(qn("w:fill"), fill)


def set_cell_margins(cell, top=80, start=90, bottom=80, end=90):
    tc = cell._tc
    tc_pr = tc.get_or_add_tcPr()
    tc_mar = tc_pr.first_child_found_in("w:tcMar")
    if tc_mar is None:
        tc_mar = OxmlElement("w:tcMar")
        tc_pr.append(tc_mar)
    for m, v in (("top", top), ("start", start), ("bottom", bottom), ("end", end)):
        node = tc_mar.find(qn(f"w:{m}"))
        if node is None:
            node = OxmlElement(f"w:{m}")
            tc_mar.append(node)
        node.set(qn("w:w"), str(v))
        node.set(qn("w:type"), "dxa")


def set_cell_text(cell, text, bold=False, size=Pt(10), align=WD_ALIGN_PARAGRAPH.LEFT, color=None):
    cell.text = ""
    p = cell.paragraphs[0]
    p.alignment = align
    p.paragraph_format.space_after = Pt(0)
    p.paragraph_format.line_spacing = 1.0
    r = p.add_run(str(text))
    font_for(r, size, bold=bold)
    if color:
        r.font.color.rgb = color
    set_cell_margins(cell)
    cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
    return p


def add_page_field(paragraph):
    run = paragraph.add_run()
    fld_char1 = OxmlElement("w:fldChar")
    fld_char1.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = " PAGE "
    fld_char2 = OxmlElement("w:fldChar")
    fld_char2.set(qn("w:fldCharType"), "end")
    run._r.append(fld_char1)
    run._r.append(instr)
    run._r.append(fld_char2)
    font_for(run, Pt(9))


def add_footer(section):
    footer = section.footer
    p = footer.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(0)
    r = p.add_run("FinTrack Project Report  |  ")
    font_for(r, Pt(9))
    add_page_field(p)


def configure_document(doc):
    section = doc.sections[0]
    section.page_width = Cm(21.0)
    section.page_height = Cm(29.7)
    section.top_margin = Cm(1.7)
    section.bottom_margin = Cm(1.5)
    section.left_margin = Cm(1.8)
    section.right_margin = Cm(1.6)
    add_footer(section)

    normal = doc.styles["Normal"]
    normal.font.name = FONT
    normal._element.rPr.rFonts.set(qn("w:eastAsia"), FONT)
    normal.font.size = BODY
    normal.paragraph_format.line_spacing = 1.5
    normal.paragraph_format.space_after = Pt(5)
    normal.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY

    for style_name, size, align in (("Heading 1", 16, WD_ALIGN_PARAGRAPH.CENTER), ("Heading 2", 14, WD_ALIGN_PARAGRAPH.LEFT), ("Heading 3", 12, WD_ALIGN_PARAGRAPH.LEFT)):
        style = doc.styles[style_name]
        style.font.name = FONT
        style._element.rPr.rFonts.set(qn("w:eastAsia"), FONT)
        style.font.size = Pt(size)
        style.font.bold = True
        style.paragraph_format.line_spacing = 1.0
        style.paragraph_format.space_before = Pt(5)
        style.paragraph_format.space_after = Pt(5)
        style.paragraph_format.alignment = align


def add_para(doc, text="", bold=False, italic=False, align=WD_ALIGN_PARAGRAPH.JUSTIFY, size=BODY, spacing=1.5, after=5, before=0):
    p = doc.add_paragraph()
    p.alignment = align
    p.paragraph_format.line_spacing = spacing
    p.paragraph_format.space_after = Pt(after)
    p.paragraph_format.space_before = Pt(before)
    r = p.add_run(text)
    font_for(r, size, bold=bold, italic=italic)
    return p


def add_rich_para(doc, parts, align=WD_ALIGN_PARAGRAPH.JUSTIFY, spacing=1.5, after=5, before=0):
    p = doc.add_paragraph()
    p.alignment = align
    p.paragraph_format.line_spacing = spacing
    p.paragraph_format.space_after = Pt(after)
    p.paragraph_format.space_before = Pt(before)
    for text, bold, italic in parts:
        r = p.add_run(text)
        font_for(r, BODY, bold=bold, italic=italic)
    return p


def heading(doc, text, level=2, align=None):
    p = doc.add_paragraph()
    p.style = f"Heading {level}"
    if align is not None:
        p.alignment = align
    r = p.add_run(text)
    font_for(r, Pt(16 if level == 1 else 14 if level == 2 else 12), bold=True)
    return p


def chapter_title(doc, number, title):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(3)
    r = p.add_run(number)
    font_for(r, Pt(16), bold=True)
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(14)
    r = p.add_run(title.upper())
    font_for(r, Pt(16), bold=True)


def bullet(doc, text, level=0):
    p = doc.add_paragraph(style="List Bullet")
    p.paragraph_format.left_indent = Cm(0.5 + level * 0.4)
    p.paragraph_format.first_line_indent = Cm(-0.25)
    p.paragraph_format.line_spacing = 1.25
    p.paragraph_format.space_after = Pt(2)
    r = p.add_run(text)
    font_for(r, BODY)
    return p


def caption(doc, text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.line_spacing = 1.0
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(5)
    r = p.add_run(text)
    font_for(r, CAPTION, bold=True)
    return p


def add_table(doc, headers, rows, widths=None, font_size=9):
    table = doc.add_table(rows=1, cols=len(headers))
    table.style = "Table Grid"
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = True
    for i, h in enumerate(headers):
        set_cell_text(table.rows[0].cells[i], h, bold=True, size=Pt(font_size), align=WD_ALIGN_PARAGRAPH.CENTER)
        set_cell_shading(table.rows[0].cells[i], "D9EAD3")
    for row in rows:
        cells = table.add_row().cells
        for i, value in enumerate(row):
            set_cell_text(cells[i], value, size=Pt(font_size))
    if widths:
        for row in table.rows:
            for i, width in enumerate(widths):
                row.cells[i].width = Inches(width)
    return table


def add_image(doc, path, width=None, height=None):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(2)
    run = p.add_run()
    run.add_picture(str(path), width=Inches(width) if width else None, height=Inches(height) if height else None)
    return p


def add_images_side_by_side(doc, paths, widths):
    table = doc.add_table(rows=1, cols=len(paths))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = True
    for i, (path, width) in enumerate(zip(paths, widths)):
        cell = table.rows[0].cells[i]
        cell.text = ""
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_after = Pt(0)
        p.add_run().add_picture(str(path), width=Inches(width))
        set_cell_margins(cell, top=20, start=20, bottom=20, end=20)
    return table


def new_page(doc):
    doc.add_page_break()


def make_font(path, size, bold=False):
    candidates = [
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf",
    ]
    for candidate in candidates:
        if Path(candidate).exists():
            return ImageFont.truetype(candidate, size)
    return ImageFont.load_default()


def rounded_box(draw, xy, fill, outline, radius=18, width=3):
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)


def centered_text(draw, box, text, font, fill):
    x1, y1, x2, y2 = box
    bbox = draw.multiline_textbbox((0, 0), text, font=font, align="center", spacing=5)
    w = bbox[2] - bbox[0]
    h = bbox[3] - bbox[1]
    draw.multiline_text(((x1 + x2 - w) / 2, (y1 + y2 - h) / 2), text, font=font, fill=fill, align="center", spacing=5)


def arrow(draw, start, end, fill="#355c4d", width=5):
    draw.line([start, end], fill=fill, width=width)
    import math
    angle = math.atan2(end[1] - start[1], end[0] - start[0])
    length = 18
    a1 = angle + 2.65
    a2 = angle - 2.65
    p1 = (end[0] + length * math.cos(a1), end[1] + length * math.sin(a1))
    p2 = (end[0] + length * math.cos(a2), end[1] + length * math.sin(a2))
    draw.polygon([end, p1, p2], fill=fill)


def make_diagrams():
    ASSETS.mkdir(parents=True, exist_ok=True)
    bg = "#f6f3ea"
    ink = "#173f34"
    green = "#d8ec9a"
    pale = "#e3f1e8"
    mid = "#a8d0bb"
    white = "#fffdf8"
    title_font = make_font("", 38, True)
    box_font = make_font("", 25, True)
    small_font = make_font("", 21, False)

    im = Image.new("RGB", (1800, 900), bg)
    d = ImageDraw.Draw(im)
    d.text((70, 45), "FinTrack end-to-end architecture", font=title_font, fill=ink)
    boxes = [
        (70, 260, 350, 560, "Browser\nHTML/CSS/JS", green),
        (470, 260, 750, 560, "Fetch API\nBearer token", pale),
        (870, 175, 1180, 645, "Spring Boot\nSecurity + REST\nControllers", mid),
        (1310, 260, 1570, 560, "JPA / Hibernate\nRepositories", pale),
        (1650, 260, 1775, 560, "MySQL\nInnoDB", green),
    ]
    for x1, y1, x2, y2, text, fill in boxes:
        rounded_box(d, (x1, y1, x2, y2), fill, ink)
        centered_text(d, (x1, y1, x2, y2), text, box_font, ink)
    arrow(d, (350, 410), (470, 410))
    arrow(d, (750, 410), (870, 410))
    arrow(d, (1180, 410), (1310, 410))
    arrow(d, (1570, 410), (1650, 410))
    d.text((495, 690), "Validated DTOs • service rules • scoped ownership • aggregate queries • mapped responses", font=small_font, fill=ink)
    im.save(ASSETS / "architecture.png")

    im = Image.new("RGB", (1800, 650), bg)
    d = ImageDraw.Draw(im)
    d.text((70, 35), "Graphical abstract: from a money event to an informed decision", font=title_font, fill=ink)
    boxes2 = [
        (70, 210, 380, 500, "1\nRecord\nincome or\nexpense", green),
        (500, 210, 810, 500, "2\nValidate\nand secure\nthe request", pale),
        (930, 210, 1240, 500, "3\nAggregate\nspending,\nbudgets, goals", mid),
        (1360, 210, 1730, 500, "4\nAct with\nclarity\nDashboard / report", green),
    ]
    for x1, y1, x2, y2, text, fill in boxes2:
        rounded_box(d, (x1, y1, x2, y2), fill, ink)
        centered_text(d, (x1, y1, x2, y2), text, box_font, ink)
    for a, b in [((380, 355), (500, 355)), ((810, 355), (930, 355)), ((1240, 355), (1360, 355))]:
        arrow(d, a, b)
    im.save(ASSETS / "graphical_abstract.png")


def build():
    make_diagrams()
    doc = Document()
    configure_document(doc)

    # PAGE 1 — TITLE PAGE
    add_para(doc, "PROJECT REPORT", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(18), spacing=1.0, after=22)
    add_para(doc, "FINTRACK", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(28), spacing=1.0, after=5)
    add_para(doc, "PERSONAL FINANCE MANAGEMENT SYSTEM", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(16), spacing=1.0, after=22)
    add_para(doc, "A secured full-stack web application for tracking income, expenses, budgets and savings goals", align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(13), spacing=1.5, after=28)
    add_para(doc, "Submitted by", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(14), spacing=1.0, after=10)
    add_para(doc, "KAUSTAV ROY\nUID: 24BCS11538\n\nHEMANT SINGH\nUID: 24BCS10030", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(14), spacing=1.5, after=25)
    add_para(doc, "Under the guidance of\n\n________________________________________\nSUPERVISOR\n\nDepartment of Computer Science and Engineering\n________________________________________\nInstitution / University", align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(12), spacing=1.3, after=15)
    add_para(doc, "NOVEMBER 2026", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(13), spacing=1.0, after=0)

    # PAGE 2 — CERTIFICATE
    new_page(doc)
    add_para(doc, "BONAFIDE CERTIFICATE", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(16), spacing=1.0, after=22)
    add_para(doc, "This is to certify that the project report entitled “FinTrack — Personal Finance Management System” is a bonafide record of the work carried out by Kaustav Roy (UID: 24BCS11538) and Hemant Singh (UID: 24BCS10030) in partial fulfillment of the academic requirements for the project evaluation during the academic session 2026.", spacing=2.0, after=18)
    add_para(doc, "The work presented in this report describes the analysis, design, implementation and validation of a multi-user personal finance application. The project integrates a responsive HTML/CSS/JavaScript interface with a secured Java Spring Boot REST backend and a MySQL relational database.", spacing=2.0, after=18)
    add_para(doc, "The work has been completed under my supervision and guidance. To the best of my knowledge, the report has not been submitted elsewhere for the award of any other qualification.", spacing=2.0, after=30)
    add_para(doc, "Date: ____________________\nPlace: ____________________", spacing=1.5, after=25)
    add_para(doc, "Signature: ______________________________\n\n________________________________________\nSUPERVISOR\n\nAcademic Designation: ____________________\nDepartment: _____________________________\nInstitution: _____________________________", align=WD_ALIGN_PARAGRAPH.LEFT, spacing=1.5, after=0)

    # PAGE 3 — ACKNOWLEDGEMENT
    new_page(doc)
    add_para(doc, "ACKNOWLEDGEMENT", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(16), spacing=1.0, after=22)
    add_para(doc, "We express our sincere gratitude to our supervisor for the guidance, review and encouragement provided throughout the development of FinTrack. The feedback received during problem definition, design selection and validation helped us convert an initial personal finance idea into a working full-stack application.", spacing=2.0, after=18)
    add_para(doc, "We thank the faculty members and the Department of Computer Science and Engineering for providing the academic environment, computing facilities and technical direction required to complete this project. We are also grateful to the open-source communities behind Java, Spring Boot, MySQL, Bootstrap, Chart.js and the other tools used in this work.", spacing=2.0, after=18)
    add_para(doc, "Finally, we acknowledge the support of our classmates, friends and family members who offered suggestions while the interface, documentation and demonstration workflow were refined. Their comments encouraged us to focus on clarity, usability and reliable backend integration.", spacing=2.0, after=25)
    add_para(doc, "\n\nKaustav Roy\nHemant Singh", align=WD_ALIGN_PARAGRAPH.RIGHT, spacing=1.5, after=0)

    # PAGE 4 — CONTENTS
    new_page(doc)
    add_para(doc, "TABLE OF CONTENTS", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(16), spacing=1.0, after=15)
    toc_rows = [
        ("Acknowledgement", "iii"), ("Table of Contents", "iv"), ("List of Figures", "v"), ("List of Tables", "v"),
        ("Abstract", "vi"), ("Graphical Abstract", "vii"), ("Abbreviations, Symbols and Nomenclature", "viii"),
        ("Chapter 1: Introduction", "9"), ("Chapter 2: Literature Survey", "10"), ("Chapter 3: Design Flow / Process", "11"),
        ("Chapter 4: Results, Analysis and Validation", "13"), ("Chapter 5: Conclusion and Future Work", "15"),
        ("References", "16"), ("Appendix A: User Manual", "17"), ("Appendix B: Achievements and Contribution", "20"),
    ]
    table = add_table(doc, ["CONTENTS", "PAGE"], toc_rows, widths=[5.4, 0.7], font_size=10)
    for row in table.rows[1:]:
        row.cells[1].paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
    add_para(doc, "Note: Title page and bonafide certificate are not listed as report contents, consistent with the supplied project-report format.", italic=True, size=Pt(10), spacing=1.0, after=0)

    # PAGE 5 — LISTS
    new_page(doc)
    add_para(doc, "LIST OF FIGURES", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(16), spacing=1.0, after=10)
    figures = [
        ("Figure 1", "FinTrack end-to-end architecture", "12"),
        ("Figure 2", "FinTrack dashboard with API-derived financial summary", "13"),
        ("Figure 3", "Transaction search and history interface", "14"),
        ("Figure 4", "Sign-in page used to enter the protected workspace", "17"),
        ("Figure 5", "Budget monitoring and threshold states", "19"),
        ("Figure 6", "Report generation and export interface", "19"),
        ("Figure 7", "Responsive mobile dashboard", "19"),
    ]
    add_table(doc, ["NUMBER", "CAPTION", "PAGE"], figures, widths=[0.9, 4.7, 0.6], font_size=9)
    add_para(doc, "LIST OF TABLES", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(16), spacing=1.0, before=10, after=10)
    tables = [
        ("Table 1", "Technology stack and role", "13"),
        ("Table 2", "Alternative designs evaluated", "11"),
        ("Table 3", "Core data model", "12"),
        ("Table 4", "Verification and validation results", "14"),
        ("Table 5", "Project achievements and contribution", "20"),
    ]
    add_table(doc, ["NUMBER", "CAPTION", "PAGE"], tables, widths=[0.9, 4.7, 0.6], font_size=9)

    # PAGE 6 — ABSTRACT
    new_page(doc)
    add_para(doc, "ABSTRACT", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(16), spacing=1.0, after=22)
    abstract = (
        "FinTrack is a multi-user personal finance management system developed to help individuals record financial activity, understand spending patterns, plan monthly limits and monitor savings goals. The application responds to a common problem: personal financial information is often distributed across notebooks, spreadsheets and disconnected mobile applications, making it difficult to obtain a reliable and timely view of income, expenses and future plans."
    )
    add_para(doc, abstract, spacing=2.0, after=15)
    add_para(doc, "The project uses a responsive HTML5/CSS3/JavaScript frontend and a Java 17 Spring Boot backend. The backend exposes validated REST endpoints, stores records in MySQL through Spring Data JPA and Hibernate, and protects user data with BCrypt password hashing, signed JSON Web Tokens and server-side session revocation. Dashboard and report values are calculated from backend aggregates rather than being stored as duplicated browser state. The interface includes transactions, categories, budgets, savings goals, charts, reports and CSV exports.", spacing=2.0, after=15)
    add_para(doc, "The design was implemented as a layered monolith so that the complete data path remains understandable and testable: browser, Fetch API, Spring Security, controllers, services, repositories, Hibernate/JDBC and MySQL. Validation included 30 automated JUnit/Mockito/MockMvc cases, JavaScript syntax checks, responsive Chromium rendering, API login and dashboard smoke tests, CORS verification and logout-token revocation checks. The result is a functional academic project that demonstrates Java/OOP, Spring Boot, REST, database integration and full-stack development in one coherent system.", spacing=2.0, after=18)
    add_rich_para(doc, [("Keywords: ", True, False), ("personal finance, Spring Boot, Java, REST API, MySQL, JWT, budgeting, savings goals, Chart.js, responsive web application.", False, False)], spacing=1.5, after=0)

    # PAGE 7 — GRAPHICAL ABSTRACT
    new_page(doc)
    add_para(doc, "GRAPHICAL ABSTRACT", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(16), spacing=1.0, after=13)
    add_image(doc, ASSETS / "graphical_abstract.png", width=6.55)
    caption(doc, "Graphical Abstract: FinTrack decision cycle")
    add_para(doc, "The project converts a simple financial event into a validated and useful decision cycle. A user records income or expense data in the browser. The request is authenticated and validated by the Spring Boot API, persisted through JPA/Hibernate and then included in aggregate views such as balance, budget usage, category spending and savings progress.", spacing=1.5, after=8)
    add_para(doc, "This flow keeps the browser responsible for interaction and presentation while the backend remains authoritative for ownership, money calculations, business rules and exports.", spacing=1.5, after=0)

    # PAGE 8 — ABBREVIATIONS
    new_page(doc)
    add_para(doc, "ABBREVIATIONS, SYMBOLS AND NOMENCLATURE", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(16), spacing=1.0, after=15)
    abbrev = [
        ("API", "Application Programming Interface"), ("BCrypt", "Adaptive password hashing algorithm"), ("CORS", "Cross-Origin Resource Sharing"),
        ("CRUD", "Create, Read, Update and Delete"), ("CSV", "Comma-Separated Values"), ("DB", "Database"),
        ("DTO", "Data Transfer Object"), ("H2", "Embedded Java relational database used in tests"), ("HTTP", "Hypertext Transfer Protocol"),
        ("INR / ₹", "Indian Rupee"), ("JPA", "Java Persistence API"), ("JWT", "JSON Web Token"),
        ("ORM", "Object-Relational Mapping"), ("REST", "Representational State Transfer"), ("UI / UX", "User Interface / User Experience"),
    ]
    add_table(doc, ["TERM", "MEANING"], abbrev, widths=[1.2, 5.0], font_size=9)
    add_para(doc, "Symbols", bold=True, size=Pt(14), spacing=1.0, before=12, after=5)
    add_table(doc, ["SYMBOL", "USE IN THIS REPORT"], [("₹", "Currency values are represented in Indian rupees."), ("%", "Percentage used for savings rate, progress and budget usage."), ("→", "Direction of data flow in architecture and process diagrams."), ("/", "Separates alternative values or closely related terms.")], widths=[0.9, 5.3], font_size=9)

    # PAGE 9 — CHAPTER 1
    new_page(doc)
    chapter_title(doc, "CHAPTER 1", "INTRODUCTION")
    heading(doc, "1.1 Identification of Client and Need", 2)
    add_para(doc, "The intended client is an individual earner, student or household member who needs a private and understandable view of day-to-day finances. The user needs to record income and expenses, classify transactions, set realistic category limits and monitor progress towards goals without maintaining several separate spreadsheets.")
    heading(doc, "1.2 Relevant Contemporary Issues", 2)
    add_para(doc, "Digital payments have made financial activity frequent and fragmented. At the same time, users expect mobile-friendly interfaces, secure access and immediate summaries. A system that only stores transactions does not solve the full problem; it must also provide trustworthy calculations, useful warnings, searchable history and exportable evidence.")
    heading(doc, "1.3 Problem Identification", 2)
    add_para(doc, "The problem addressed by FinTrack is the absence of a small, integrated workspace that combines transaction capture, budget monitoring, manual savings goals and reports while preserving user ownership and secure authentication. Client-side-only storage is insufficient for multi-user privacy, persistence and trustworthy aggregation.")
    heading(doc, "1.4 Task Identification and Timeline", 2)
    add_table(doc, ["PHASE", "MAJOR TASK", "OUTPUT"], [("1", "Requirement analysis and domain modelling", "Features, entities and rules"), ("2", "Backend and database implementation", "REST API, JPA mappings, security"), ("3", "Frontend and charts", "Responsive pages and API rendering"), ("4", "Testing, redesign and documentation", "Validated build, screenshots and report")], widths=[0.6, 3.2, 2.4], font_size=8)
    heading(doc, "1.5 Organization of the Report", 2)
    add_para(doc, "Chapter 2 surveys related financial-management and secure web-application approaches. Chapter 3 explains alternatives, constraints, architecture and implementation flow. Chapter 4 presents implementation results and validation. Chapter 5 concludes the work and identifies future extensions. Appendices provide a user manual and achievements.")

    # PAGE 10 — CHAPTER 2
    new_page(doc)
    chapter_title(doc, "CHAPTER 2", "LITERATURE SURVEY")
    heading(doc, "2.1 Development Timeline of the Reported Problem", 2)
    add_para(doc, "Personal finance tools evolved from paper ledgers to desktop spreadsheets, then to mobile banking and cloud-based dashboards. Early tools focused on recording transactions. Later systems introduced categorization, budgets, charts and notifications. Current expectations add privacy, mobile responsiveness, data export, explainable calculations and secure multi-user access.")
    heading(doc, "2.2 Thematic Bibliometric Analysis", 2)
    add_para(doc, "A qualitative mapping of selected standards, framework documentation and financial-literacy sources was performed using the themes of financial awareness, secure authentication, web architecture, persistence, visualization and usability. The mapping shows that a useful academic project should combine these themes instead of treating finance calculation, security and interface design as independent modules.")
    add_table(doc, ["THEME", "OBSERVATION FROM SELECTED SOURCES", "PROJECT RESPONSE"], [("Financial awareness", "Users benefit from categorization, planning and feedback.", "Budgets, goals, reports and dashboard summaries."), ("Security", "Authentication and authorization must be separate concerns.", "BCrypt, JWT verification, scoped queries and revocation."), ("Web engineering", "Layered APIs improve maintainability and testing.", "Controllers, services, repositories and DTOs."), ("Visualization", "Aggregated information is easier to interpret than raw rows.", "Chart.js cash-flow and category charts."), ("Usability", "Responsive and accessible interactions reduce friction.", "Mobile layout, labels, dialogs and keyboard support.")], widths=[1.2, 2.8, 2.2], font_size=8)
    heading(doc, "2.3 Proposed Solutions and Link to FinTrack", 2)
    add_para(doc, "Proposed solutions commonly combine financial-literacy education, automated categorization, budgeting feedback and secure digital records. FinTrack adopts the practical academic subset: user-controlled categories, transparent calculations, budget warnings and explicit goal progress.", spacing=1.2, after=2)
    heading(doc, "2.4 Problem Definition, Goals and Objectives", 2)
    add_para(doc, "Problem definition: build a secure, responsive and testable workspace that stores user-owned records and turns them into actionable summaries. Goals are trustworthy CRUD, clear planning tools and a maintainable Java full-stack architecture; objectives cover authentication, finance modules, reports, exports, responsive UI and validation.", spacing=1.15, after=1)

    # PAGE 11 — CHAPTER 3A
    new_page(doc)
    chapter_title(doc, "CHAPTER 3", "DESIGN FLOW / PROCESS")
    heading(doc, "3.1 Concept Generation", 2)
    add_para(doc, "Three concepts were considered. The first was a spreadsheet-style local tool focused on manual entry. The second was a client-side dashboard storing JSON in browser storage. The third was a secured full-stack application with a Java API, relational database and responsive frontend. The third concept was selected because it satisfies persistence, privacy, ownership and academic integration requirements together.")
    heading(doc, "3.2 Alternative Designs and Selection", 2)
    alternatives = [("A: Local spreadsheet", "Fast to prototype; familiar", "Weak validation, sharing and security", "Rejected"), ("B: Browser-only storage", "Simple deployment; no server", "Records tied to one browser; weak multi-user model", "Rejected"), ("C: Layered Spring Boot monolith", "Secure, testable and explainable; persistent data", "Requires Java, Maven and MySQL setup", "Selected")]
    add_table(doc, ["DESIGN", "STRENGTHS", "LIMITATIONS", "DECISION"], alternatives, widths=[1.2, 1.9, 2.5, 0.8], font_size=8)
    caption(doc, "Table 2: Alternative designs evaluated")
    heading(doc, "3.3 Feature Evaluation and Finalization", 2)
    add_para(doc, "Features were prioritized by user value, technical feasibility and evidence value for evaluation. Transactions form the base record; categories provide controlled classification; budgets provide constraints; goals provide forward-looking planning; reports provide analysis; security and validation protect every protected action. Charts were added only where aggregate data could be explained clearly.")
    heading(doc, "3.4 Design Constraints", 2)
    add_para(doc, "The project is constrained by academic time, local deployment resources and the need for transparent implementation. Professional and ethical constraints require that passwords are hashed, records are scoped to the authenticated user, exports are real, error responses do not leak stack traces and the UI does not claim financial advice. Economic and environmental impact is reduced by using a small layered deployment and bounded export pages. Health, safety and political risks are not materially introduced by the software, but privacy and responsible use remain explicit concerns.")

    # PAGE 12 — CHAPTER 3B
    new_page(doc)
    chapter_title(doc, "CHAPTER 3", "DESIGN FLOW / PROCESS")
    heading(doc, "3.5 Selected Architecture and Data Flow", 2)
    add_image(doc, ASSETS / "architecture.png", width=6.55)
    caption(doc, "Figure 1: FinTrack end-to-end architecture")
    add_para(doc, "The selected layered monolith separates presentation, transport, security, business rules, persistence and relational storage without introducing unnecessary distributed-system complexity. Response records are mapped from entities so passwords, sessions and persistence relationships do not leak into the browser.", spacing=1.2, after=3)
    heading(doc, "3.6 Core Data Model", 2)
    model_rows = [("users", "Identity, email, BCrypt hash and profile"), ("categories", "User-owned labels with INCOME, EXPENSE or BOTH type"), ("transactions", "Amount, type, category, payment, date and description"), ("budgets", "Category limit for one user and month/year"), ("financial_goals", "Target, saved amount and deadline"), ("auth_sessions", "JWT session ID, owner and expiry for revocation")]
    add_table(doc, ["TABLE", "PURPOSE"], model_rows, widths=[1.6, 4.6], font_size=8)
    caption(doc, "Table 3: Core data model")
    heading(doc, "3.7 Implementation Algorithm", 2)
    add_para(doc, "For a protected request, the browser sends a bearer token and JSON body. The JWT filter validates signature, issuer, expiry and active session. The controller binds and validates a request DTO. The service checks ownership and domain rules, then invokes a repository within a transaction. The mapper returns a password-free response. The page re-renders from the response and, where required, refreshes related aggregates.", spacing=1.3, after=0)

    # PAGE 13 — CHAPTER 4A
    new_page(doc)
    chapter_title(doc, "CHAPTER 4", "RESULTS, ANALYSIS AND VALIDATION")
    heading(doc, "4.1 Implementation Using Modern Engineering Tools", 2)
    add_table(doc, ["LAYER", "TOOLS", "IMPLEMENTATION ROLE"], [("Frontend", "HTML5, CSS3, JavaScript ES modules, Bootstrap 5.3.3", "Responsive pages, forms, dialogs and state rendering"), ("Charts / HTTP", "Chart.js 4.4.7, Fetch API, async/await", "Visual aggregates and JSON/CSV communication"), ("Backend", "Java 17, Spring Boot 3.5.6, Spring MVC", "REST controllers, validation and embedded server"), ("Persistence", "Spring Data JPA, Hibernate, MySQL 8", "Entities, repositories, constraints and queries"), ("Security", "Spring Security, BCrypt, JJWT", "Password hashing, JWT authentication and revocation"), ("Testing", "JUnit 5, Mockito, MockMvc, H2, Maven", "Unit, integration and build verification")], widths=[1.0, 2.5, 2.7], font_size=8)
    caption(doc, "Table 1: Technology stack and role")
    heading(doc, "4.2 Dashboard Result", 2)
    add_para(doc, "The dashboard combines a balance card, income, expenses and savings rate with current-month spending, charts, recent activity, budget warnings and goal progress. The following capture was produced from the running frontend and API-backed demo data.", spacing=1.3, after=5)
    add_image(doc, ROOT / "screenshots" / "dashboard-light.png", width=5.35)
    caption(doc, "Figure 2: FinTrack dashboard with API-derived financial summary")

    # PAGE 14 — CHAPTER 4B
    new_page(doc)
    chapter_title(doc, "CHAPTER 4", "RESULTS, ANALYSIS AND VALIDATION")
    heading(doc, "4.3 Transaction and Reporting Results", 2)
    add_para(doc, "The transaction page supports server-side search, category/type/payment/date filters, sorting, pagination, editing, deletion and CSV export. The reports page derives monthly, category and date-range summaries from backend data. These results demonstrate that the interface is not a static mockup: visible records and totals are fetched from the Java API.", spacing=1.3, after=5)
    add_image(doc, ROOT / "screenshots" / "transactions.png", width=3.95)
    caption(doc, "Figure 3: Transaction search and history interface")
    heading(doc, "4.4 Verification and Data Validation", 2)
    validation_rows = [("Backend automated suite", "30", "0 failures, 0 errors, 0 skipped"), ("Build verification", "mvn verify", "Spring Boot executable JAR generated"), ("Frontend syntax", "13 modules", "node --check passed"), ("API smoke test", "Login + dashboard", "Token, summary, monthly and recent data passed"), ("Security smoke test", "Logout + old token", "204 logout; revoked token rejected with 401"), ("Browser pages", "6 protected pages", "Dashboard, transactions, budgets, goals, reports and profile loaded")]
    add_table(doc, ["CHECK", "SCOPE", "RESULT"], validation_rows, widths=[1.8, 1.4, 3.0], font_size=8)
    caption(doc, "Table 4: Verification and validation results")

    # PAGE 15 — CHAPTER 5
    new_page(doc)
    chapter_title(doc, "CHAPTER 5", "CONCLUSION AND FUTURE WORK")
    heading(doc, "5.1 Conclusion", 2)
    add_para(doc, "FinTrack achieved the primary objective of providing a working personal finance workspace with a secure Java backend and a redesigned responsive frontend. The application accepts user-owned financial records, applies domain validation, calculates balance and planning metrics on the server, and presents those values through dashboards, charts and reports.")
    heading(doc, "5.2 Analysis of Results and Deviations", 2)
    add_para(doc, "The expected result was a coherent full-stack application rather than isolated screens. That result was achieved through the layered Spring Boot implementation, real REST integration and automated tests. The main deviation from a production banking product is deliberate: the project does not connect to bank accounts, classify transactions with machine learning or provide financial advice. These omissions keep the scope safe, explainable and suitable for an academic demonstration.")
    heading(doc, "5.3 Future Work", 2)
    bullet(doc, "Add recurring transactions, verified email and password reset workflows.")
    bullet(doc, "Introduce database migrations, backups, audit history and deployment observability.")
    bullet(doc, "Add optional bank-import adapters with explicit consent and reconciliation controls.")
    bullet(doc, "Extend reports with configurable currencies, tax categories and scheduled exports.")
    bullet(doc, "Add accessibility testing and formal usability evaluation with representative users.")
    heading(doc, "5.4 Way Ahead", 2)
    add_para(doc, "The current layered design provides a stable foundation for these extensions. New features can be added behind services and DTOs without changing the fundamental ownership model or replacing the browser interface. The project therefore meets its academic goal while leaving a realistic path toward a more complete personal finance product.")

    # PAGE 16 — REFERENCES
    new_page(doc)
    add_para(doc, "REFERENCES", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=Pt(16), spacing=1.0, after=20)
    refs = [
        "Chart.js Contributors (2024), Chart.js Documentation, https://www.chartjs.org/docs/latest/.",
        "Hibernate ORM Team (2025), Hibernate ORM User Guide, https://hibernate.org/orm/documentation/.",
        "MySQL Documentation Team (2025), MySQL 8.0 Reference Manual, Oracle Corporation.",
        "OWASP Foundation (2024), JSON Web Token for Java Cheat Sheet, OWASP Developer Guide.",
        "RBI (2020), National Strategy for Financial Education 2020–2025, Reserve Bank of India.",
        "Spring Team (2025), Spring Boot Reference Documentation 3.5, https://docs.spring.io/spring-boot/.",
        "Spring Team (2025), Spring Security Reference, https://docs.spring.io/spring-security/reference/.",
        "Oracle (2024), Java Platform, Standard Edition Documentation, Java 17 API Specification.",
        "Bootstrap Contributors (2024), Bootstrap 5.3 Documentation, https://getbootstrap.com/docs/5.3/.",
        "Project source and verification records (2026), FinTrack repository documentation: architecture, API, database design and testing guide.",
    ]
    for i, ref in enumerate(refs, 1):
        add_para(doc, f"{i}. {ref}", align=WD_ALIGN_PARAGRAPH.LEFT, size=Pt(11), spacing=1.0, after=7)
    add_para(doc, "Note: Online documentation links are included for technical references. The report uses the project source code, test reports and captured application screens as primary implementation evidence.", italic=True, size=Pt(10), spacing=1.0, before=12, after=0)

    # PAGE 17 — APPENDIX A1
    new_page(doc)
    chapter_title(doc, "APPENDIX A", "USER MANUAL")
    heading(doc, "A.1 Prerequisites", 2)
    add_para(doc, "Install JDK 17, Maven 3.9 or later, MySQL 8 and a modern browser. Import database/schema.sql into a fresh MySQL database and create the application account as described in README.md. Configure DB_USERNAME, DB_PASSWORD, JWT_SECRET and CORS_ORIGINS before starting the API.")
    heading(doc, "A.2 Start the Application", 2)
    bullet(doc, "Open a terminal in FinTrack/backend and run: mvn spring-boot:run")
    bullet(doc, "Open another terminal in FinTrack/frontend and run: py -m http.server 5500 --bind 127.0.0.1")
    bullet(doc, "Open http://localhost:5500/index.html in the browser.")
    heading(doc, "A.3 Create an Account or Use Demo Data", 2)
    add_para(doc, "Select Create your free account, enter a name, email and password, then submit. The password must contain 10–72 printable ASCII characters with uppercase, lowercase and a number. For an opt-in demonstration account, enable the backend demo seed and use the credentials documented in README.md.")
    heading(doc, "A.4 Login and Navigation", 2)
    add_para(doc, "Use the login page to obtain a JWT-backed session. The protected pages are Overview, Transactions, Budgets, Savings goals, Reports and Profile & categories. On a small screen, use the menu button to open the sidebar. Ctrl+K or Command+K opens quick navigation.")
    add_image(doc, ROOT / "screenshots" / "login.png", width=5.15)
    caption(doc, "Figure 4: Sign-in page used to enter the protected workspace")

    # PAGE 18 — APPENDIX A2
    new_page(doc)
    chapter_title(doc, "APPENDIX A", "USER MANUAL")
    heading(doc, "A.5 Dashboard and Transactions", 2)
    add_para(doc, "After login, review the balance, income, expenses and savings rate. Use the Add transaction button to record an income or expense. Choose a compatible category, amount, payment method and date. Save the form and confirm that the new row appears in Transaction history. Use Edit to replace values, or Delete and confirm the native dialog to remove a record.")
    add_para(doc, "The transaction filters are sent to the server. Search by description or category, restrict type/category/payment/date, change sorting and move through pages. Export CSV downloads all matching records rather than only the visible page. The workflow is shown in Figure 3.", spacing=1.2, after=3)
    heading(doc, "A.6 Profile and Categories", 2)
    add_para(doc, "Open Profile & categories to update the name, email or optional phone number. Create a personal category with type INCOME, EXPENSE or BOTH. A category that is already referenced by a transaction or budget cannot be deleted, protecting financial history.")

    # PAGE 19 — APPENDIX A3
    new_page(doc)
    chapter_title(doc, "APPENDIX A", "USER MANUAL")
    heading(doc, "A.7 Budgets, Goals and Reports", 2)
    add_para(doc, "On Budgets, select a month and create an expense-category limit. The application calculates spent, remaining and usage from transactions; NORMAL is below 75%, WARNING is 75–<90%, CRITICAL is 90–100% and EXCEEDED is above 100%.", spacing=1.2, after=2)
    add_para(doc, "On Savings goals, enter a target, saved amount and deadline. On Reports, select a month or custom date range and generate the report or CSV export.", spacing=1.2, after=2)
    add_images_side_by_side(doc, [ROOT / "screenshots" / "budgets.png", ROOT / "screenshots" / "reports.png"], [2.25, 2.25])
    caption(doc, "Figure 5: Budget monitoring and threshold states")
    caption(doc, "Figure 6: Report generation and export interface")
    add_para(doc, "At mobile widths the sidebar becomes a drawer, cards collapse and tables remain readable through internal scrolling.", spacing=1.1, after=1)
    add_image(doc, ROOT / "screenshots" / "mobile-360.png", width=1.0)
    caption(doc, "Figure 7: Responsive mobile dashboard")

    # PAGE 20 — APPENDIX B
    new_page(doc)
    chapter_title(doc, "APPENDIX B", "ACHIEVEMENTS AND CONTRIBUTION")
    heading(doc, "B.1 Achievements", 2)
    achievements = [("Functional scope", "Authentication, transactions, categories, budgets, goals, reports, CSV export and responsive UI"), ("Security", "BCrypt password hashing, JWT validation, scoped ownership and durable logout revocation"), ("Testing", "30 automated tests plus frontend syntax, API smoke, CORS, browser rendering and responsive checks"), ("Documentation", "Architecture, API, database, testing, Java/OOP and FSD evaluation notes"), ("Design", "Dark emerald Horizon frontend redesign with light/dark themes and mobile navigation")]
    add_table(doc, ["AREA", "EVIDENCE"], achievements, widths=[1.5, 4.7], font_size=8)
    caption(doc, "Table 5: Project achievements and contribution")
    heading(doc, "B.2 Contributor Declaration", 2)
    add_para(doc, "We declare that this report presents the work completed for the FinTrack project and that the implementation evidence, screenshots and test results correspond to the project files supplied with this report. The two contributors jointly participated in requirement analysis, design, implementation, testing and documentation.")
    add_para(doc, "\n\nKaustav Roy\nUID: 24BCS11538\nSignature: ____________________\n\nHemant Singh\nUID: 24BCS10030\nSignature: ____________________", align=WD_ALIGN_PARAGRAPH.LEFT, spacing=1.5, after=0)

    # Apply consistent formatting to every table paragraph and remove accidental empty runs.
    for table in doc.tables:
        for row in table.rows:
            for cell in row.cells:
                for p in cell.paragraphs:
                    p.paragraph_format.line_spacing = 1.0
                    p.paragraph_format.space_after = Pt(0)

    doc.core_properties.title = "FinTrack — Personal Finance Management System Project Report"
    doc.core_properties.subject = "Project report prepared according to supplied project report format"
    doc.core_properties.author = "Kaustav Roy; Hemant Singh"
    doc.core_properties.keywords = "FinTrack, personal finance, Spring Boot, Java, MySQL, project report"
    doc.save(OUT)
    print(OUT)


if __name__ == "__main__":
    build()
