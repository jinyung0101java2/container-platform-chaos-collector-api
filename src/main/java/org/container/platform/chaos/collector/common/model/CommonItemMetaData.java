package org.container.platform.chaos.collector.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CommonItemMetaData 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonItemMetaData {

    private Integer allItemCount;
    private Integer remainingItemCount;

}