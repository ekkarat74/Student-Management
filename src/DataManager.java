import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.BiFunction; 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.nio.charset.StandardCharsets;

public class DataManager {

    private static final String JSON_FILE = "students.json";
    private static final String XML_FILE = "students.xml";

    public List<Student> loadFromFile(File f) {
        List<Student> list = new ArrayList<>();
        try (Scanner sc = new Scanner(f, StandardCharsets.UTF_8)) {
            while (sc.hasNextLine()) {
                String[] d = sc.nextLine().split(",");
                 // ⭐️⭐️⭐️ (จุดที่แก้ไข) ⭐️⭐️⭐️
                if (d.length >= 9) {
                     // ⭐️ แก้ไข: เรียกใช้ 11-arg constructor (ตัด d[8] ที่เป็น dateAdded ออก)
                     list.add(new Student(d[0], d[1], "N/A", "N/A", d[5], "N/A", 
                                          Integer.parseInt(d[2]), Double.parseDouble(d[3]),
                                          Integer.parseInt(d[6].replace("+", "")),
                                          StudentStatus.valueOf(d[7]), d[4]));
                } else if (d.length >= 7) {
                    // ⭐️ แก้ไข: เรียกใช้ 11-arg constructor (ตัด d[6] ที่เป็น dateAdded ออก)
                     list.add(new Student(d[0], d[1], "N/A", "N/A", d[5], "N/A", 
                                          Integer.parseInt(d[2]), Double.parseDouble(d[3]),
                                          1, StudentStatus.ENROLLED, d[4]));
                } else if (d.length >= 5) {
                    // ⭐️ (โค้ดนี้ถูกต้องอยู่แล้ว)
                     list.add(new Student(d[0], d[1], "N/A", "N/A", "N/A", "N/A", 
                                          Integer.parseInt(d[2]), Double.parseDouble(d[3]),
                                          1, StudentStatus.ENROLLED, d[4]));
                }
                // ⭐️⭐️⭐️ (จบจุดที่แก้ไข) ⭐️⭐️⭐️
            }
        } catch (Exception e) { e.printStackTrace(); return null; }
        return list;
    }

    public List<Student> loadFromCsv(File f) {
        List<Student> list = new ArrayList<>();
        try (Scanner sc = new Scanner(f, StandardCharsets.UTF_8)) {
            if (sc.hasNextLine()) sc.nextLine(); // Skip Header
            while (sc.hasNextLine()) {
                String[] d = sc.nextLine().split(",");
                // ⭐️⭐️⭐️ (จุดที่แก้ไข) ⭐️⭐️⭐️
                if (d.length >= 9) {
                    // ⭐️ แก้ไข: เรียกใช้ 11-arg constructor (ตัด d[8] ที่เป็น dateAdded ออก)
                    list.add(new Student(d[0], d[1], "N/A", "N/A", d[5], "N/A", 
                                         Integer.parseInt(d[2]), Double.parseDouble(d[3]),
                                         Integer.parseInt(d[6].replace("+", "")),
                                         StudentStatus.valueOf(d[7]), d[4]));
                }
                // ⭐️⭐️⭐️ (จบจุดที่แก้ไข) ⭐️⭐️⭐️
            }
        } catch (Exception e) { e.printStackTrace(); return null; }
        return list;
    }

    public List<Student> loadFromXml(File f) {
        List<Student> list = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(f);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("student");
            
            BiFunction<Element, String, String> getText = (e, tag) -> 
                e.getElementsByTagName(tag).getLength() > 0 ? e.getElementsByTagName(tag).item(0).getTextContent() : "N/A";

            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    String id = getText.apply(e, "id");
                    String name = getText.apply(e, "name");
                    String address = getText.apply(e, "address");
                    String phone = getText.apply(e, "phone");
                    String email = getText.apply(e, "email");
                    String photoPath = getText.apply(e, "photoPath");
                    int age = Integer.parseInt(getText.apply(e, "age").equals("N/A") ? "0" : getText.apply(e, "age"));
                    double gpa = Double.parseDouble(getText.apply(e, "gpa").equals("N/A") ? "0.0" : getText.apply(e, "gpa"));
                    int year = Integer.parseInt(getText.apply(e, "year").equals("N/A") ? "1" : getText.apply(e, "year").replace("+", ""));
                    StudentStatus status = StudentStatus.valueOf(getText.apply(e, "status").equals("N/A") ? "ENROLLED" : getText.apply(e, "status"));
                    String major = getText.apply(e, "major");
                    String date = getText.apply(e, "dateAdded"); // ⭐️ เราดึง date มา แต่...
                    
                    // ⭐️⭐️⭐️ (จุดที่แก้ไข) ⭐️⭐️⭐️
                    // ⭐️ แก้ไข: เรียกใช้ 11-arg constructor (ตัด date ที่เพิ่งดึงมา ออก)
                    list.add(new Student(id, name, address, phone, email, photoPath, age, gpa, year, status, major));
                    // ⭐️⭐️⭐️ (จบจุดที่แก้ไข) ⭐️⭐️⭐️
                }
            }
        } catch (Exception e) { e.printStackTrace(); return null; }
        return list;
    }

    public boolean exportToJson(List<Student> students) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
        new FileOutputStream(JSON_FILE), StandardCharsets.UTF_8))) {
            writer.println("[");
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                writer.printf(
                        "  {\"id\":\"%s\", \"name\":\"%s\", \"address\":\"%s\", \"phone\":\"%s\", \"email\":\"%s\", \"photoPath\":\"%s\", \"age\":%d, \"gpa\":%.2f, \"major\":\"%s\", \"year\":%d, \"status\":\"%s\", \"dateAdded\":\"%s\", \"previousSchool\":\"%s\"}%s%n",
                        s.id, s.name, s.address, s.phone, s.email, s.photoPath, 
                        s.age, s.gpa, s.major, s.year, s.status.name(), s.dateAdded,
                        s.previousSchool,
                        (i < students.size() - 1 ? "," : "")
                );
            }
            writer.println("]");
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    public boolean exportToXml(List<Student> students) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            DocumentBuilder b = f.newDocumentBuilder();
            Document doc = b.newDocument();
            Element root = doc.createElement("students");
            doc.appendChild(root);
            for (Student s : students) {
                Element e = doc.createElement("student");
                root.appendChild(e);
                BiConsumer<String, String> createChild = (tag, value) -> {
                    Element child = doc.createElement(tag);
                    child.appendChild(doc.createTextNode(value != null ? value : ""));
                    e.appendChild(child);
                };
                createChild.accept("id", s.id);
                createChild.accept("name", s.name);
                createChild.accept("address", s.address); 
                createChild.accept("phone", s.phone);    
                createChild.accept("email", s.email);
                createChild.accept("photoPath", s.photoPath); 
                createChild.accept("age", String.valueOf(s.age));
                createChild.accept("gpa", String.valueOf(s.gpa));
                createChild.accept("major", s.major);
                createChild.accept("year", String.valueOf(s.year));
                createChild.accept("status", s.status.name());
                createChild.accept("dateAdded", s.dateAdded);
                createChild.accept("previousSchool", s.previousSchool);
                createChild.accept("docApplication", s.docApplicationPath);
                createChild.accept("docIdCard", s.docIdCardPath);
                createChild.accept("docTranscript", s.docTranscriptPath);
            }
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(
                new FileOutputStream(XML_FILE), StandardCharsets.UTF_8
            )));
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}