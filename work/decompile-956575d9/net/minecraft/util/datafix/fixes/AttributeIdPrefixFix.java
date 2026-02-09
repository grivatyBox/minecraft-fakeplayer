package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import java.util.List;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class AttributeIdPrefixFix extends AttributesRenameFix {

    private static final List<String> PREFIXES = List.of("generic.", "horse.", "player.", "zombie.");

    public AttributeIdPrefixFix(Schema schema) {
        super(schema, "AttributeIdPrefixFix", AttributeIdPrefixFix::replaceId);
    }

    private static String replaceId(String s) {
        String s1 = DataConverterSchemaNamed.ensureNamespaced(s);

        for (String s2 : AttributeIdPrefixFix.PREFIXES) {
            String s3 = DataConverterSchemaNamed.ensureNamespaced(s2);

            if (s1.startsWith(s3)) {
                String s4 = s1.substring(s3.length());

                return "minecraft:" + s4;
            }
        }

        return s;
    }
}
