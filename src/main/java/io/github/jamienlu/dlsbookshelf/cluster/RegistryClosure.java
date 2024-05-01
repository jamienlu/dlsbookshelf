package io.github.jamienlu.dlsbookshelf.cluster;

import com.alipay.sofa.jraft.Closure;
import io.github.jamienlu.dlsbookshelf.api.RegistryResult;
import io.github.jamienlu.dlsbookshelf.api.RegistryServiceOperate;
import lombok.Data;

/**
 * @author jamieLu
 * @create 2024-04-28
 */
@Data
public abstract class RegistryClosure implements Closure {
    private RegistryServiceOperate registryServiceOperate;
    private RegistryResult registryResult;
    public void success(Object data) {
        registryResult.setStatus(true);
        registryResult.setData(data);
    }

    public void failure(Object data, String redirect) {
        registryResult.setStatus(false);
        if (null != redirect) {
            registryResult.setRedirect(redirect);
        }
    }
}
