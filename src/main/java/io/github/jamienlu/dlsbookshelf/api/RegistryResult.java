package io.github.jamienlu.dlsbookshelf.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author jamieLu
 * @create 2024-04-28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistryResult implements Serializable {
    private boolean status;
    private Object data;
    private String redirect;
}
