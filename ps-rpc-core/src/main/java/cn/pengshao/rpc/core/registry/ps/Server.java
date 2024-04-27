package cn.pengshao.rpc.core.registry.ps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Description:Registry server instance
 *
 * @Author: yezp
 * @date 2024/4/17 22:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"url"})
public class Server {

    private String url;
    private boolean status;
    private boolean leader;
    private long version;

}
