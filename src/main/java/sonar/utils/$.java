package sonar.utils;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

@Log4j2
@UtilityClass
@SuppressWarnings("all")
public class $ {

    private static Map<String, Properties> propertiesCache = new HashMap<>();


    public static String stringFormat(String src, String placeholder, Object... args) {
        checkNonNull(src, placeholder, args);
        checkNonBlank(src, placeholder);
        val srcArr = src.toCharArray();
        val phArr = placeholder.toCharArray();
        val allPhPosition = new ArrayList<Integer>();
        // 第一步：先把占位符的位置都收集到一个数组中
        for (int i = 0; i < srcArr.length; i++) {
            boolean isFind = true;
            for (int j = 0; j < phArr.length && i + j < srcArr.length; j++) {
                if (srcArr[i + j] != phArr[j]) {
                    isFind = false;
                    break;
                }
            }
            if (isFind) {
                allPhPosition.add(i);
                i+=phArr.length;
            }
        }
        // 第二步：判段占位符个数与传入的参数数量是否相等
        if (allPhPosition.size() != args.length) throw new IllegalArgumentException("占位符个数与传入的参数数量不匹配");
        // 第三步：计算最终生成的字符串长度
        int len = srcArr.length - allPhPosition.size() * phArr.length;
        for (int i = 0; i < args.length; i++) {
            len += args[i].toString().length();
        }
        // 第四步：拼接字符串
        char[] ret = new char[len];
        int theNextTimeSrcArrStartPosition = 0;
        int theNextTimeDstArrStartPosition = 0;
        for (int i = 0; i < args.length; i++) {
            int len1 = allPhPosition.get(i)-theNextTimeSrcArrStartPosition;
            System.arraycopy(srcArr, theNextTimeSrcArrStartPosition, ret, theNextTimeDstArrStartPosition, len1);
            theNextTimeSrcArrStartPosition = allPhPosition.get(i) + phArr.length;
            theNextTimeDstArrStartPosition += len1;
            char[] value = args[i].toString().toCharArray();
            System.arraycopy(value, 0, ret, theNextTimeDstArrStartPosition, value.length);
            theNextTimeDstArrStartPosition += value.length;
        }
        System.arraycopy(srcArr, theNextTimeSrcArrStartPosition, ret, theNextTimeDstArrStartPosition, src.length()-theNextTimeSrcArrStartPosition);
        return new String(ret);
    }

    public static Try<String> readFileAsStringFromClassPath(String path) {
        checkNonNull(path);
        checkNonBlank(path);
        final InputStream is = $.class.getResourceAsStream(path);
        val ret = new StringBuilder();
        return Try.of(() -> {
            val br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            br.lines().forEach(l -> ret.append(l));
            return ret.toString();
        });
    }

    public static Try<String> readFileAsString(String path) {
        checkNonBlank(path);
        checkNonNull(path);

        val file = new File(path);
        val ret = new StringBuilder();
        return Try.of(() -> {
            val br = new BufferedReader(new FileReader(file));
            br.lines().forEach(l -> ret.append(l));
            return ret.toString();
        });
    }

    /**
     * @param filePath 配置文件在类路径下的地址
     * @return Properties对象或<tt>NULL</tt>
     */
    public static Properties getProperties(String filePath) {
        checkNonNull(filePath);
        checkNonBlank(filePath);

        if (propertiesCache.get(filePath) != null) return propertiesCache.get(filePath);

        Properties res = new Properties();
        try (InputStream is = $.class.getResourceAsStream(filePath)) {
            res.load(is);
            propertiesCache.put(filePath, res);
            return res;
        } catch (IOException e) {
            log.error("读取properties文件出现异常", e);
        }
        throw new IllegalStateException("从配置文件【" + filePath + "】中加载配置失败！");
    }

    /**
     * 获取properties文件指定的key对应的value值，获取不到就返回默认值。Properties中有getOrDefault方法，但仅限于JDK1.8。
     *
     * @param p          Properties
     * @param key        Properties文件的key
     * @param defaultVal 获取不到key时返回的默认值
     * @param <T>        类型根据传入默认值来推断
     * @return value值或默认值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrDefault(Properties p, String key, T defaultVal) {
        checkNonNull(p, key, defaultVal);
        checkNonBlank(key);

        String value = p.getProperty(key);
        if (value == null) return defaultVal;

        if (defaultVal instanceof Integer) {
            return (T) new Integer(value);
        } else if (defaultVal instanceof Long) {
            return (T) new Long(value);
        } else if (defaultVal instanceof Double) {
            return (T) new Double(value);
        } else if (defaultVal instanceof Float) {
            return (T) new Float(value);
        } else if (defaultVal instanceof Boolean) {
            return (T) new Boolean(value);
        } else if (defaultVal instanceof Short) {
            return (T) new Short(value);
        } else if (defaultVal instanceof Byte) {
            return (T) new Byte(value);
        } else if (defaultVal instanceof String) {
            return (T) value;
        }
        throw new IllegalStateException("无法解析非基础类型的数据");
    }

    public static void checkNonNull(Object... args) {
        for (Object obj : args) {
            Objects.requireNonNull(obj);
        }
    }

    public static void checkNonBlank(String... args) {
        for (String s : args) {
            if (s.length() == 0)
                throw new IllegalArgumentException("不能传入空字符串");
        }
    }
}
