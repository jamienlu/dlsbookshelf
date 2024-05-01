package io.github.jamienlu.dlsbookshelf.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author jamieLu
 * @create 2024-04-28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistryServiceOperate implements Serializable {
    private String methodName;
    private List<Class<?>> paramsType;
    private Object[] args;
}
