package core.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @className: core.cache-> Util
 * @description: 工具
 * @author: raoty
 * @Date: 2023-04-11 20:52
 * @version: V1.0
 */
public class Util {
    public static boolean isNull(Object obj) {
        boolean res = false;
        if (obj == null) {
            res = true;
        }else if (obj instanceof String) {
            res = ((String)obj).isEmpty();
        }else if(obj instanceof Map){
            res = ((Map)obj).isEmpty();
        }else if(obj instanceof Collection){
            res = ((Collection)obj).isEmpty();
        }
        return res;
    }
}
