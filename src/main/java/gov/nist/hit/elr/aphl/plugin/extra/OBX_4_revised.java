package gov.nist.hit.elr.aphl.plugin.extra;

import java.util.ArrayList;
import java.util.Collections;

import gov.nist.hit.elr.plugin.utils.CodedElement;
import hl7.v2.instance.Element;
import hl7.v2.instance.Query;
import hl7.v2.instance.Simple;
import scala.collection.Iterator;
import scala.collection.immutable.List;

/**
 * Revised OBX-4 check (GVT 1.3.7, 2026-03-13).
 *
 * Differs from {@link OBX_4} in what counts as "the same OBX-3": the identifier
 * pair (OBX-3.1, OBX-3.3) and the alternate pair (OBX-3.4, OBX-3.6) are counted
 * separately instead of as one composite, and only the pair whose code system
 * is not local ("L") decides, falling back to the local pair when there is no
 * other. This is the class the PHLIP 2026 profiles (V2_IG2, PHLIP_v20260715,
 * DASH_v20260715) reference.
 */
public class OBX_4_revised {

  /**
   * OBX-4 condition predicate/conformance statement: if there are multiple OBX
   * segments associated with the same OBR segment that carry the same OBX-3
   * value, OBX-4 is required on each of them.
   *
   * @param e the ORDER_OBSERVATION group
   * @return the custom messages, one per OBX in error
   */
  public java.util.List<String> assertionWithCustomMessages(Element e) {
    java.util.List<String> messages = new java.util.ArrayList<String>();

    List<Element> OBXList = Query.query(e, "6[*].1[1]").get();
    if (OBXList == null || OBXList.size() == 0) {
      // no OBX, we can move on, no check performed
      return messages;
    }
    java.util.List<CodedElement> _OBX3s = new ArrayList<CodedElement>();
    Iterator<Element> it = OBXList.iterator();
    while (it.hasNext()) {
      Element OBX = it.next();
      // OBX-3
      List<Simple> OBX3_1List = Query.queryAsSimple(OBX, "3[1].1[1]").get();
      List<Simple> OBX3_3List = Query.queryAsSimple(OBX, "3[1].3[1]").get();
      List<Simple> OBX3_4List = Query.queryAsSimple(OBX, "3[1].4[1]").get();
      List<Simple> OBX3_6List = Query.queryAsSimple(OBX, "3[1].6[1]").get();

      String OBX3_1 = OBX3_1List.size() > 0 ? OBX3_1List.apply(0).value().raw() : "";
      String OBX3_3 = OBX3_3List.size() > 0 ? OBX3_3List.apply(0).value().raw() : "";
      String OBX3_4 = OBX3_4List.size() > 0 ? OBX3_4List.apply(0).value().raw() : "";
      String OBX3_6 = OBX3_6List.size() > 0 ? OBX3_6List.apply(0).value().raw() : "";

      CodedElement identifierOBX3 = new CodedElement(OBX3_1, OBX3_3);
      CodedElement alternateOBX3 = new CodedElement(OBX3_4, OBX3_6);

      if (!identifierOBX3.isEmpty()) {
        _OBX3s.add(identifierOBX3);
      }
      if (!alternateOBX3.isEmpty()) {
        _OBX3s.add(alternateOBX3);
      }
    }

    Iterator<Element> it2 = OBXList.iterator();
    while (it2.hasNext()) {
      Element OBX = it2.next();
      // OBX-3
      List<Simple> OBX3_1List = Query.queryAsSimple(OBX, "3[1].1[1]").get();
      List<Simple> OBX3_3List = Query.queryAsSimple(OBX, "3[1].3[1]").get();
      List<Simple> OBX3_4List = Query.queryAsSimple(OBX, "3[1].4[1]").get();
      List<Simple> OBX3_6List = Query.queryAsSimple(OBX, "3[1].6[1]").get();

      String OBX3_1 = OBX3_1List.size() > 0 ? OBX3_1List.apply(0).value().raw() : "";
      String OBX3_3 = OBX3_3List.size() > 0 ? OBX3_3List.apply(0).value().raw() : "";
      String OBX3_4 = OBX3_4List.size() > 0 ? OBX3_4List.apply(0).value().raw() : "";
      String OBX3_6 = OBX3_6List.size() > 0 ? OBX3_6List.apply(0).value().raw() : "";

      CodedElement identifierOBX3 = new CodedElement(OBX3_1, OBX3_3);
      CodedElement alternateOBX3 = new CodedElement(OBX3_4, OBX3_6);

      // OBX-4
      List<Simple> OBX4List = Query.queryAsSimple(OBX, "4[1]").get();
      String OBX4 = OBX4List.size() > 0 ? OBX4List.apply(0).value().raw() : "";

      messages.addAll(check(_OBX3s, identifierOBX3, alternateOBX3, OBX4));
    }
    return messages;
  }

  /**
   * @param obx3s every non-empty OBX-3 pair (identifier and alternate) under the
   *        same OBR
   * @param obx3_1 the identifier pair (OBX-3.1, OBX-3.3) of the OBX being checked
   * @param obx3_4 the alternate pair (OBX-3.4, OBX-3.6) of the OBX being checked
   * @param obx4 the OBX-4 value, empty when absent
   * @return the messages for this OBX
   */
  public java.util.List<String> check(java.util.List<CodedElement> obx3s, CodedElement obx3_1,
      CodedElement obx3_4, String obx4) {
    java.util.List<String> messages = new java.util.ArrayList<String>();
    int count_1 = Collections.frequency(obx3s, obx3_1);
    int count_4 = Collections.frequency(obx3s, obx3_4);

    if (isStandard(obx3_1.getCodeSystem())) {
      if (count_1 > 1 && "".equals(obx4)) {
        messages.add("OBX-4 is required with OBX-3 (" + obx3_1.prettyPrint() + ")");
      }
    } else if (isStandard(obx3_4.getCodeSystem())) {
      if (count_4 > 1 && "".equals(obx4)) {
        messages.add("OBX-4 is required with OBX-3 (" + obx3_4.prettyPrint() + ")");
      }
    } else if ("L".equals(obx3_1.getCodeSystem())) {
      if (count_1 > 1 && "".equals(obx4)) {
        messages.add("OBX-4 is required with OBX-3 (" + obx3_1.prettyPrint() + ")");
      }
    } else if ("L".equals(obx3_4.getCodeSystem())) {
      if (count_4 > 1 && "".equals(obx4)) {
        messages.add("OBX-4 is required with OBX-3 (" + obx3_4.prettyPrint() + ")");
      }
    }
    return messages;
  }

  /**
   * A code system that is neither local ("L") nor empty: PLT, LN or any other
   * named system.
   */
  private static boolean isStandard(String codeSystem) {
    return "PLT".equals(codeSystem) || "LN".equals(codeSystem)
        || (!"L".equals(codeSystem) && !"".equals(codeSystem));
  }
}
