package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.util.MathHelper;
import net.minecraft.world.phys.Vec3D;

public class EntityAttachments {

    private final Map<EntityAttachment, List<Vec3D>> attachments;

    EntityAttachments(Map<EntityAttachment, List<Vec3D>> map) {
        this.attachments = map;
    }

    public static EntityAttachments createDefault(float f, float f1) {
        return builder().build(f, f1);
    }

    public static EntityAttachments.a builder() {
        return new EntityAttachments.a();
    }

    public EntityAttachments scale(float f, float f1, float f2) {
        return new EntityAttachments(SystemUtils.makeEnumMap(EntityAttachment.class, (entityattachment) -> {
            List<Vec3D> list = new ArrayList();

            for (Vec3D vec3d : (List) this.attachments.get(entityattachment)) {
                list.add(vec3d.multiply((double) f, (double) f1, (double) f2));
            }

            return list;
        }));
    }

    @Nullable
    public Vec3D getNullable(EntityAttachment entityattachment, int i, float f) {
        List<Vec3D> list = (List) this.attachments.get(entityattachment);

        return i >= 0 && i < list.size() ? transformPoint((Vec3D) list.get(i), f) : null;
    }

    public Vec3D get(EntityAttachment entityattachment, int i, float f) {
        Vec3D vec3d = this.getNullable(entityattachment, i, f);

        if (vec3d == null) {
            String s = String.valueOf(entityattachment);

            throw new IllegalStateException("Had no attachment point of type: " + s + " for index: " + i);
        } else {
            return vec3d;
        }
    }

    public Vec3D getClamped(EntityAttachment entityattachment, int i, float f) {
        List<Vec3D> list = (List) this.attachments.get(entityattachment);

        if (list.isEmpty()) {
            throw new IllegalStateException("Had no attachment points of type: " + String.valueOf(entityattachment));
        } else {
            Vec3D vec3d = (Vec3D) list.get(MathHelper.clamp(i, 0, list.size() - 1));

            return transformPoint(vec3d, f);
        }
    }

    private static Vec3D transformPoint(Vec3D vec3d, float f) {
        return vec3d.yRot(-f * ((float) Math.PI / 180F));
    }

    public static class a {

        private final Map<EntityAttachment, List<Vec3D>> attachments = new EnumMap(EntityAttachment.class);

        a() {}

        public EntityAttachments.a attach(EntityAttachment entityattachment, float f, float f1, float f2) {
            return this.attach(entityattachment, new Vec3D((double) f, (double) f1, (double) f2));
        }

        public EntityAttachments.a attach(EntityAttachment entityattachment, Vec3D vec3d) {
            ((List) this.attachments.computeIfAbsent(entityattachment, (entityattachment1) -> {
                return new ArrayList(1);
            })).add(vec3d);
            return this;
        }

        public EntityAttachments build(float f, float f1) {
            Map<EntityAttachment, List<Vec3D>> map = SystemUtils.<EntityAttachment, List<Vec3D>>makeEnumMap(EntityAttachment.class, (entityattachment) -> {
                List<Vec3D> list = (List) this.attachments.get(entityattachment);

                return list == null ? entityattachment.createFallbackPoints(f, f1) : List.copyOf(list);
            });

            return new EntityAttachments(map);
        }
    }
}
