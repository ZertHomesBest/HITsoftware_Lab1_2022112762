import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Set;

public class TextToGraphTest {
    
    private TextToGraph textToGraph;
    private final String testFilePath = "./test/junit_test.txt";
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    
    @BeforeEach
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
        
        // 重定向标准输出以捕获打印内容
        System.setOut(new PrintStream(outContent));
    }
    
    @AfterEach
    public void tearDown() {
        // 在每个测试方法执行后删除测试文件并恢复标准输出
        File testFile = new File(testFilePath);
        if (testFile.exists()) {
            testFile.delete();
        }
        System.setOut(originalOut);
    }
    
    // 测试用例1：有效等价类 (1,3,5) - 起始单词和目标单词都存在于图中，存在桥接词，需要打印结果
    @Test
    @DisplayName("测试 - 有效等价类 (1,3,5)")
    public void testValidClass135() {
        // 清空输出缓冲区
        outContent.reset();
        
        // 执行查询 - "the" 到 "fox" 之间有桥接词 "quick"
        Set<String> bridgeWords = textToGraph.queryBridgeWords("the", "fox", true);
        
        // 验证结果
        assertNotNull(bridgeWords, "桥接词集合不应为空");
        assertFalse(bridgeWords.isEmpty(), "应该存在桥接词");
        assertTrue(bridgeWords.contains("quick"), "桥接词应包含 'quick'");
        
        // 验证输出内容
        String output = outContent.toString();
        assertTrue(output.contains("The bridge words from the to fox are:"), 
                  "输出应包含正确的桥接词信息");
          
          // 添加这一行来显示实际输出内容
          System.setOut(originalOut);
          System.out.println("测试用例1的实际输出：\n" + output);
    }
    
    // 测试用例2：无效等价类 (2,3,5) - 起始单词不存在于图中，需要打印结果
    @Test
    @DisplayName("测试 - 无效等价类 (2,3,5) - 起始单词不存在")
    public void testInvalidClass235StartWord() {
        // 清空输出缓冲区
        outContent.reset();
        
        // 执行查询 - "nonexistent" 不存在于图中
        Set<String> bridgeWords = textToGraph.queryBridgeWords("nonexistent", "fox", true);
        
        // 验证结果
        assertNull(bridgeWords, "桥接词集合应为null");
        
        // 验证输出内容
        String output = outContent.toString();
        assertTrue(output.contains("在图中没有\"nonexistent\""), 
                  "输出应提示起始单词不存在");
        // 添加这一行来显示实际输出内容
        System.setOut(originalOut);
        System.out.println("测试用例2的实际输出：\n" + output);
    }
    
    // 测试用例3：无效等价类 (2,3,5) - 目标单词不存在于图中，需要打印结果
    @Test
    @DisplayName("测试 - 无效等价类 (2,3,5) - 目标单词不存在")
    public void testInvalidClass235EndWord() {
        // 清空输出缓冲区
        outContent.reset();
        
        // 执行查询 - "nonexistent" 不存在于图中
        Set<String> bridgeWords = textToGraph.queryBridgeWords("the", "nonexistent", true);
        
        // 验证结果
        assertNull(bridgeWords, "桥接词集合应为null");
        
        // 验证输出内容
        String output = outContent.toString();
        assertTrue(output.contains("在图中没有\"nonexistent\""), 
                  "输出应提示目标单词不存在");
        // 添加这一行来显示实际输出内容
        System.setOut(originalOut);
        System.out.println("测试用例3的实际输出：\n" + output);
    }
    
    // 测试用例4：无效等价类 (1,4,5) - 起始单词和目标单词都存在于图中，不存在桥接词，需要打印结果
    @Test
    @DisplayName("测试 - 无效等价类 (1,4,5)")
    public void testInvalidClass145() {
        // 清空输出缓冲区
        outContent.reset();
        
        // 执行查询 - "fox" 到 "the" 之间没有桥接词
        Set<String> bridgeWords = textToGraph.queryBridgeWords("fox", "the", true);
        
        // 验证结果
        assertNotNull(bridgeWords, "桥接词集合不应为null");
        assertTrue(bridgeWords.isEmpty(), "桥接词集合应为空");
        
        // 验证输出内容
        String output = outContent.toString();
        assertTrue(output.contains("fox和the之间没有桥接词"), 
                  "输出应提示不存在桥接词");
        // 添加这一行来显示实际输出内容
        System.setOut(originalOut);
        System.out.println("测试用例4的实际输出：\n" + output);
    }
    
    // 测试用例5：有效等价类 (1,3) 和无效等价类 (6) - 起始单词和目标单词都存在于图中，存在桥接词，不需要打印结果
    @Test
    @DisplayName("测试 - 有效等价类 (1,3) 和无效等价类 (6)")
    public void testValidClass13InvalidClass6() {
        // 清空输出缓冲区
        outContent.reset();
        
        // 执行查询 - "the" 到 "fox" 之间有桥接词，但不打印结果
        Set<String> bridgeWords = textToGraph.queryBridgeWords("the", "fox", false);
        
        // 验证结果
        assertNotNull(bridgeWords, "桥接词集合不应为空");
        assertFalse(bridgeWords.isEmpty(), "应该存在桥接词");
        assertTrue(bridgeWords.contains("quick"), "桥接词应包含 'quick'");
        
        // 验证没有输出内容
        String output = outContent.toString();
        assertTrue(output.isEmpty(), "不应有任何输出");
    }
    
    // 测试用例6：无效等价类 (1,4,6) - 起始单词和目标单词都存在于图中，不存在桥接词，不需要打印结果
    @Test
    @DisplayName("测试 - 无效等价类 (1,4,6)")
    public void testInvalidClass146() {
        // 清空输出缓冲区
        outContent.reset();
        
        // 执行查询 - "fox" 到 "the" 之间没有桥接词，且不打印结果
        Set<String> bridgeWords = textToGraph.queryBridgeWords("fox", "the", false);
        
        // 验证结果
        assertNotNull(bridgeWords, "桥接词集合不应为null");
        assertTrue(bridgeWords.isEmpty(), "桥接词集合应为空");
        
        // 验证没有输出内容
        String output = outContent.toString();
        assertTrue(output.isEmpty(), "不应有任何输出");
    }
}