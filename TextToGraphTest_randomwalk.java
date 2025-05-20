import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TextToGraphTest_randomwalk {
    
    private TextToGraph textToGraph;
    private TextToGraph textToGraph2;
    private final String testFilePath = "./test/junit_test.txt";
    private final String outputPath = "./random_walk.txt";
    
    @Before
    public void setUp() throws IOException {
        // 在每个测试方法执行前创建 TextToGraph 实例和测试文件
        textToGraph = new TextToGraph();
        
        // 创建测试目录
        File testDir = new File("./test");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        
        // 创建测试文件
        File testFile = new File(testFilePath);
        try (PrintWriter writer = new PrintWriter(testFile, "UTF-8")) {
            writer.println("the quick brown fox jumps over the lazy dog");
            writer.println("the dog barks at the fox");
            writer.println("quick fox jumps high");
        }
        
        // 构建有向图
        textToGraph.buildDirectedGraph(testFilePath);
    }
    
    @After
    public void tearDown() {
        // 在每个测试方法执行后删除测试文件和输出文件
        File testFile = new File(testFilePath);
        if (testFile.exists()) {
            testFile.delete();
        }
        
        // 暂时注释掉删除输出文件的代码，以便查看文件内容
        // File outputFile = new File(outputPath);
        // if (outputFile.exists()) {
        //     outputFile.delete();
        // }
    }
    
    // 测试用例1：测试外部停止随机游走
    @Test
    public void testRandomWalkExternalStop() throws InterruptedException {
        // 创建一个线程执行随机游走
        Thread walkThread = new Thread(() -> {
            textToGraph.randomWalk();
        });
        
        // 启动线程
        walkThread.start();
        
        // 等待一段时间后停止游走
        Thread.sleep(2000);
        
        // 使用反射访问私有变量
        try {
            java.lang.reflect.Field field = TextToGraph.class.getDeclaredField("stopWalk");
            field.setAccessible(true);
            field.set(textToGraph, true);
        } catch (Exception e) {
            fail("无法设置stopWalk变量: " + e.getMessage());
        }
        
        // 等待线程结束
        walkThread.join(1000);
        
        // 验证输出文件存在
        File outputFile = new File(outputPath);
        assertTrue("输出文件应该存在", outputFile.exists());
        
        // 验证文件内容
        try {
            List<String> lines = Files.readAllLines(Paths.get(outputPath));
            assertTrue("文件应至少包含两行", lines.size() >= 2);
            assertTrue("应该有多个节点被访问", lines.get(1).split(" ").length > 1);
            
            // 打印文件内容
            System.out.println("测试用例1 - 输出文件内容：");
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            fail("读取输出文件失败: " + e.getMessage());
        }
    }
    
    // 测试用例2：测试遇到没有出边的节点停止
    @Test
    public void testRandomWalkStopsAtDeadEnd() throws IOException {

        textToGraph2 = new TextToGraph();
        // 创建包含死胡同的测试文件
        try (PrintWriter writer = new PrintWriter(new File(testFilePath), "UTF-8")) {
            writer.println("a b c d");  // 创建 a->b->c->d 的路径，d没有出边
        }
        
        // 重新构建有向图
        textToGraph2.buildDirectedGraph(testFilePath);
        
        // 执行随机游走
        String result = textToGraph2.randomWalk();
        
        // 验证结果
        assertNotNull("随机游走结果不应为空", result);
        
        // 验证输出文件存在
        File outputFile = new File(outputPath);
        assertTrue("输出文件应该存在", outputFile.exists());
        
        // 验证文件内容
        List<String> lines = Files.readAllLines(Paths.get(outputPath));
        assertTrue("文件应至少包含两行", lines.size() >= 2);
        // 游走应该在某个节点停止，最后一个节点应该是d
        String[] nodes = lines.get(1).split(" ");
        assertEquals("游走应该在d节点停止", "d", nodes[nodes.length - 1]);
        
        // 打印文件内容
        System.out.println("测试用例2 - 输出文件内容：");
        for (String line : lines) {
            System.out.println(line);
        }
    }
    
    // 测试用例3：测试线程中断
    @Test
    public void testRandomWalkThreadInterruption() throws IOException {
        // 创建一个线程执行随机游走，并在执行过程中中断它
        Thread walkThread = new Thread(() -> {
            textToGraph.randomWalk();
        });
        
        // 启动线程
        walkThread.start();
        
        // 等待一段时间后中断线程
        try {
            Thread.sleep(1000);
            walkThread.interrupt();
            walkThread.join(1000);
        } catch (InterruptedException e) {
            fail("测试线程被意外中断");
        }
        
        // 验证输出文件存在
        File outputFile = new File(outputPath);
        assertTrue("即使线程被中断，输出文件也应该存在", outputFile.exists());
        
        // 打印文件内容
        try {
            List<String> lines = Files.readAllLines(Paths.get(outputPath));
            System.out.println("测试用例3 - 输出文件内容：");
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("测试用例3 - 读取输出文件失败: " + e.getMessage());
        }
    }
    
    // 测试用例4：测试IO异常
    @Test
    public void testRandomWalkIOException() throws IOException {
        // 创建一个不可写的目录，使得写入文件时发生IO异常
        File outputDir = new File(outputPath);
        if (outputDir.exists()) {
            outputDir.delete();
        }
        outputDir.mkdir();  // 创建同名目录，使得无法创建同名文件
        
        try {
            // 执行随机游走
            String result = textToGraph.randomWalk();
            
            // 验证结果
            assertEquals("即使发生IO异常，也应返回正确的文件路径", outputPath, result);
            
            // 尝试打印文件内容（可能会失败，因为文件可能无法创建）
            System.out.println("测试用例4 - 输出文件路径: " + result);
            try {
                if (new File(result).exists() && !new File(result).isDirectory()) {
                    List<String> lines = Files.readAllLines(Paths.get(result));
                    System.out.println("测试用例4 - 输出文件内容：");
                    for (String line : lines) {
                        System.out.println(line);
                    }
                } else {
                    System.out.println("测试用例4 - 输出文件不存在或是一个目录");
                }
            } catch (IOException e) {
                System.out.println("测试用例4 - 读取输出文件失败: " + e.getMessage());
            }
        } finally {
            // 清理测试环境
            if (outputDir.exists() && outputDir.isDirectory()) {
                outputDir.delete();
            }
        }
    }
}