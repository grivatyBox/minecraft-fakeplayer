package io.github.hello09x.fakeplayer.core.metadata;

import io.github.hello09x.fakeplayer.api.spi.ActionType;
import lombok.Data;

/**
 * 动作元数据
 * <p>
 * 用于持久化假人的动作状态。
 * 保存动作类型（ATTACK、MINE、MOVE 等）及其剩余次数。
 * </p>
 */
@Data
public class ActionMetadata {

    private ActionType type;
    private int remains;
    private int interval;

}
