package com.lib.http;

import com.lib.mthdone.IMethodDone;
import com.lib.mthdone.MethodTag;
import com.lib.utils.Abandon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.lib.mthdone.IMethodDone.Factory.MethodDone;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Mock数据请求类
 */
public class MockRequester implements IHttpRequester {

    /** mock错误码 */
    public final static int MOCK_ERR = 10000001;
    /*返回数据dealy的时间*/
    private final static int CALL_BACK_DELAY_TIME = 500;

    @Override
    public void init() {

    }

    @Override
    public void recycle() {

    }

    @Override
    public RequestCall request(Method method, Object callBack, Class cls, Object... objs) {
        MethodDone.doIt(this, "createData", method, callBack, cls, objs);
        return null;
    }

    @MethodTag(threadType = IMethodDone.THREAD_TYPE_THREAD)
    private void createData(Method method, Object callBack, Class cls, Object... objs) throws InstantiationException, IllegalAccessException {
        DefaultResponse response = DefaultResponse.createResponse(method, objs);
        response.setData(createClass(cls));
//        MethodDone.doIt(callBack, CALL_BACK_SUCCEED, response);
    }

    @Override
    public void setTimeOut(int timeMills) {

    }

    @Override
    public void setRootUrl(String rootUrl) {

    }


    @Target(ElementType.FIELD)
    @Retention(RUNTIME)
    public @interface MockString {
        String value() default "";
    }

    @Target(ElementType.FIELD)
    @Retention(RUNTIME)
    public @interface MockInt {
        int value() default 0;
    }

    @Target(ElementType.FIELD)
    @Retention(RUNTIME)
    public @interface MockFloat {
        float value() default 0;
    }

    @Target(ElementType.FIELD)
    @Retention(RUNTIME)
    public @interface MockList {
        int value() default 0;
    }

    @Target(ElementType.FIELD)
    @Retention(RUNTIME)
    public @interface MockObj {
        int value() default 0;
    }


    /**
     * 生成一个Class Mock数据对像
     *
     * @param type Class对像的ClassType
     * @param <T>  返回数据对像,应该与Class Type相同
     * @return 返回数据实例
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public final static <T> T createClass(Type type) throws IllegalAccessException, InstantiationException {
        if (type instanceof Class) {
            Class cls = (Class) type;
            Object data = cls.newInstance();
            List<Field> fields = getAllFiledInJsonBean(cls);
            for (Field field : fields) {
                setValueToObject(data, field);
            }
            return (T) data;
        }
        return null;
    }


    private final static void setValueToObject(Object obj, Field field) throws IllegalAccessException, InstantiationException {
        //mock string 的处理
        MockString sValue = field.getAnnotation(MockString.class);
        if (null != sValue) {
            field.setAccessible(true);
            field.set(obj, sValue.value());
            field.setAccessible(false);
            return;
        }

        //mock integer 的处理
        MockInt iValue = field.getAnnotation(MockInt.class);
        if (null != iValue) {
            field.setAccessible(true);
            field.setInt(obj, iValue.value());
            field.setAccessible(false);
            return;
        }

        //mock float 的处理
        MockFloat fValue = field.getAnnotation(MockFloat.class);
        if (null != fValue) {
            field.setAccessible(true);
            field.setFloat(obj, fValue.value());
            field.setAccessible(false);
            return;
        }

        //mock list 的处理
        MockList lists = field.getAnnotation(MockList.class);
        if (null != lists) {
            final int size = lists.value();
            ParameterizedType type = (ParameterizedType) field.getGenericType();
            Class cls = (Class) type.getActualTypeArguments()[0];
            Object data = createClass(cls);
            if (null != data) {
                List list = newList(cls, size);
                for (int i = 0; i < size; i++) {
                    list.add(data);
                }
                field.setAccessible(true);
                field.set(obj, list);
                field.setAccessible(false);
            }
            return;
        }

        //mock Object 的处理
        MockObj mockObj = field.getAnnotation(MockObj.class);
        if (null != mockObj) {
            Object data = createClass(field.getGenericType());
            field.setAccessible(true);
            field.set(obj, data);
            field.setAccessible(false);
        }
    }

    private final static <T> List<T> newList(Class<T> cls, int size) {
        return new ArrayList<T>(size);
    }


    private final static List<Field> getAllFiledInJsonBean(Class cls) {
        ArrayList<Field> fields = new ArrayList<>(20);
        while (!Object.class.equals(cls)) {
            Field[] flds = cls.getDeclaredFields();
            for (Field fld : flds) {
                if (fld.isAccessible()) {
                    if (null == fld.getAnnotation(Abandon.class)) {
                        fields.add(fld);
                    }
                } else {
                    fld.setAccessible(true);
                    if (null == fld.getAnnotation(Abandon.class)) {
                        fields.add(fld);
                    }
                    fld.setAccessible(false);
                }
            }
            cls = cls.getSuperclass();
        }
        return fields;
    }
}
