package group.flyfish.framework.cases;

import group.flyfish.framework.TestCase;

import javax.sql.DataSource;

import static group.flyfish.framework.utils.LogUtils.print;
import static group.flyfish.framework.utils.LogUtils.printResult;

/**
 * 抽象测试用例
 *
 * @param <T> 泛型
 */
public abstract class AbstractTestCase<T> implements TestCase<T> {

    protected DataSource dataSource;

    protected AbstractTestCase(DataSource dataSource) {
        this.dataSource = dataSource;
        try {
            this.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试运行逻辑
     *
     * @return 运行结果
     * @throws Exception 异常
     */
    public abstract T run() throws Exception;

    /**
     * 测试逻辑
     *
     * @return 测试结果值
     */
    @Override
    public T test() {
        Name anno = getClass().getAnnotation(Name.class);
        assert anno != null;
        String name = anno.value();
        long current = System.currentTimeMillis();
        T result = null;
        try {
            result = run();
            print("【初次执行】执行任务《{0}》用时：{1}ms", name, System.currentTimeMillis() - current);
            current = System.currentTimeMillis();
            result = run();
            return result;
        } catch (Exception e) {
            print("执行失败！{0}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            print("【正常执行】执行任务《{0}》用时：{1}ms", name, System.currentTimeMillis() - current);
            printResult(result);
        }
    }
}
