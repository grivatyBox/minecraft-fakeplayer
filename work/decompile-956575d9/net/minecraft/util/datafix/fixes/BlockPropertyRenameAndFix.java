package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;

public class BlockPropertyRenameAndFix extends AbstractBlockPropertyFix {

    private final String blockId;
    private final String oldPropertyName;
    private final String newPropertyName;
    private final UnaryOperator<String> valueFixer;

    public BlockPropertyRenameAndFix(Schema schema, String s, String s1, String s2, String s3, UnaryOperator<String> unaryoperator) {
        super(schema, s);
        this.blockId = s1;
        this.oldPropertyName = s2;
        this.newPropertyName = s3;
        this.valueFixer = unaryoperator;
    }

    @Override
    protected boolean shouldFix(String s) {
        return s.equals(this.blockId);
    }

    @Override
    protected <T> Dynamic<T> fixProperties(String s, Dynamic<T> dynamic) {
        return dynamic.renameAndFixField(this.oldPropertyName, this.newPropertyName, (dynamic1) -> {
            return dynamic1.createString((String) this.valueFixer.apply(dynamic1.asString("")));
        });
    }
}
