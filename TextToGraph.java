import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class TextToGraph {
    private final Map<String, Map<String, Integer>> directedGraph;
    private volatile boolean stopWalk = false; // 使用volatile保证可见性
    private final CountDownLatch latch = new CountDownLatch(1); // 用于协调线程

    public TextToGraph() {
        directedGraph = new HashMap<>();
    }

    public static void main(String[] args) {
        String filePath = "./test/test1.txt"; // 默认文本文件路径
        String dotFilePath = "./graph/directed_graph.dot"; // 输出的 DOT 文件路径
        String imageFilePath = "./graph/directed_graph.png"; // 输出的图像文件路径
        
        // 确保目录存在
        new File("./test").mkdirs();
        new File("./graph").mkdirs();
        
        // 添加文件选择功能
        if (args.length > 0) {
            filePath = args[0]; // 使用命令行参数指定的文件路径
        } else {
            // 提示用户选择文件
            Scanner fileScanner = new Scanner(System.in);
            System.out.println("请选择文本文件输入方式：");
            System.out.println("1. 使用默认文件 (" + filePath + ")");
            System.out.println("2. 输入文件路径");
            System.out.print("请输入选项 (1-2): ");
            
            int fileChoice = 1;
            try {
                if (fileScanner.hasNextInt()) {
                    fileChoice = fileScanner.nextInt();
                    if (fileChoice < 1 || fileChoice > 2) {
                        System.out.println("无效选项，使用默认文件。");
                        fileChoice = 1;
                    }
                } else {
                    System.out.println("无效输入，使用默认文件。");
                    fileScanner.nextLine();
                }
            } catch (Exception e) {
                System.out.println("输入错误，使用默认文件。");
            }
            
            fileScanner.nextLine(); // 清除换行符
            
            if (fileChoice == 2) {
                System.out.print("请输入文件路径: ");
                String userFilePath = fileScanner.nextLine().trim();
                if (!userFilePath.isEmpty()) {
                    filePath = userFilePath;
                    System.out.println("已选择文件: " + filePath);
                } else {
                    System.out.println("路径为空，使用默认文件。");
                }
            } else {
                System.out.println("使用默认文件: " + filePath);
            }
        }
        
        // 确保测试文件存在
        File testFile = new File(filePath);
        if (!testFile.exists()) {
            try {
                testFile.getParentFile().mkdirs();
                PrintWriter writer = new PrintWriter(testFile);
                writer.println("This is a test file. It contains some sample text for graph generation.");
                writer.println("The quick brown fox jumps over the lazy dog.");
                writer.close();
                System.out.println("创建测试文件: " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TextToGraph graphBuilder = new TextToGraph();
        String[] allwords = null;
        try {
            allwords = graphBuilder.showDirectedGraph(graphBuilder, filePath, dotFilePath, imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String root = allwords[0];
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                while(System.in.available() > 0)
                {
                    scanner.nextLine();
                }
            }
            catch (IOException ignored)
            {

            }
            System.out.println("请选择操作：");
            System.out.println("1. 桥接词查询");
            System.out.println("2. 生成新文本");
            System.out.println("3. 计算两个单词之间最短路径");
            System.out.println("4. 计算PageRank值");
            System.out.println("5. 开始随机游走");
            System.out.println("6. 退出");
            System.out.print("请输入操作编号：");

            // 添加输入检测
            int choice;
            try {
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    if (choice < 1 || choice > 6) {
                        System.out.println("错误：请输入1-6之间的数字！");
                        scanner.nextLine(); // 清除输入缓冲
                        continue;
                    }
                } else {
                    System.out.println("错误：请输入数字！");
                    scanner.nextLine(); // 清除无效输入
                    continue;
                }
            } catch (Exception e) {
                System.out.println("输入错误：" + e.getMessage());
                scanner.nextLine(); // 清除无效输入
                continue;
            }

            scanner.nextLine(); // 清除换行符
            graphBuilder.stopWalk = false;
            switch (choice) {
                case 1:
                    System.out.println("桥接词查询：");
                    System.out.print("请输入两个词语：");
                    String input = scanner.nextLine();
                    String[] words = input.split(" ");
                    if (words.length != 2) {
                        System.out.println("要求输入词语数量为2！");
                        break;
                    }
                    String word1 = words[0].toLowerCase();
                    String word2 = words[1].toLowerCase();
                    Set<String> bridgeWords = graphBuilder.queryBridgeWords(word1, word2, true);
                    break;

                case 2:
                    System.out.println("根据bridge word生成新文本：");
                    System.out.print("请输入文本：");
                    String input_sentence = scanner.nextLine();
                    graphBuilder.generateNewText(input_sentence);
                    break;

                case 3:
                    System.out.println("计算两个单词之间最短路径：");
                    System.out.print("请输入两个词语：");
                    String input_words = scanner.nextLine();
                    String[] wordss = input_words.split(" ");
                    if (wordss.length == 1) {
                        // 生成到所有节点的最短路径
                        String word11 = wordss[0].toLowerCase();
                        int index = 0;
                        for (String word22 : allwords) {
                            graphBuilder.calcShortestPath(word11, word22, ++index,root);
                        }
                    } else if (wordss.length != 2) {
                        System.out.println("要求输入词语数量为2！");
                    } else {
                        String word11 = wordss[0].toLowerCase();
                        String word22 = wordss[1].toLowerCase();
                        graphBuilder.calcShortestPath(word11, word22, 1,root);
                    }
                    break;

                case 4:
                    System.out.println("计算PageRank值：");
                    System.out.print("请输入单词：");
                    String word = scanner.nextLine().toLowerCase();
                    double pr = graphBuilder.calPageRank(word);
                    System.out.println("单词 " + word + " 的PageRank值为: " + pr);
                    break;

                case 5:
                    System.out.println("开始随机游走");
                    Thread walkThread = new Thread(() -> {
                        graphBuilder.randomWalk();
                        graphBuilder.latch.countDown(); // 通知主线程
                    });
                    walkThread.start();

                    System.out.println("输入任意键以停止随机游走：");
                    try{
                        while (walkThread.isAlive()) {
                            if (System.in.available() > 0) {
                                graphBuilder.stopWalk = true;
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        walkThread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;

                case 6:
                    System.out.println("退出程序");
                    scanner.close();
                    return;

                default:
                    System.out.println("无效的操作编号，请重新输入！");
            }
        }
    }

    // 添加节点到有向图
    public void addNode(String node) {
        directedGraph.putIfAbsent(node, new HashMap<>());
    }

    // 添加边到有向图
    public void addEdge(String source, String destination) {
        directedGraph.get(source).merge(destination, 1, Integer::sum);
    }

    // 构建有向图
    public String[] buildDirectedGraph(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        boolean rootSet = false; // 标志是否已经设置根节点
        String rootWord = null; // 根节点的单词
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append(" "); // 将每行内容添加到 content 中，并添加空格作为单词间的分隔符
            }
        }
        // 将非字母字符替换为空格，并转换为小写
        String processedContent = content.toString().replaceAll("[^a-zA-Z\\n\\r]", " ").toLowerCase();
        String[] words = processedContent.split("\\s+"); // 按空格分割单词

        for (int i = 0; i < words.length - 1; i++) {
            String currentWord = words[i];
            //System.out.println("currentWord: " + currentWord);
            String nextWord = words[i + 1];
            addNode(currentWord);
            addNode(nextWord);
            addEdge(currentWord, nextWord);
        }

        return words;
    }

    // 修改createDotFile方法以支持多条最短路径的不同颜色显示
    public void createDotFile(String dotFilePath, List<List<Object>> shortestPaths, String root) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(dotFilePath))) {
            writer.println("digraph G {");
            writer.println("  rankdir=LR;");
            writer.println("  node [shape=circle, style=filled, fillcolor=white];");
            
            // 标记根节点为红色
            if (root != null && !root.isEmpty()) {
                writer.println("  \"" + root + "\" [fillcolor=red];");
            }
            
            // 写入所有边
            for (Map.Entry<String, Map<String, Integer>> entry : directedGraph.entrySet()) {
                String vertex = entry.getKey();
                Map<String, Integer> edges = entry.getValue();
                
                for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                    String destination = edge.getKey();
                    int weight = edge.getValue();
                    writer.println("  \"" + vertex + "\" -> \"" + destination + "\" [label=\"" + weight + "\"];");
                }
            }
            
            // 为每条最短路径分配不同的颜色
            String[] colors = {"blue", "green", "purple", "orange", "brown", "pink", "cyan", "magenta", "yellow", "gray"};
            
            if (shortestPaths != null && !shortestPaths.isEmpty()) {
                for (int i = 0; i < shortestPaths.size(); i++) {
                    List<Object> pathInfo = shortestPaths.get(i);
                    List<String> path = (List<String>) pathInfo.get(0);
                    String color = colors[i % colors.length]; // 循环使用颜色
                    
                    // 标记路径上的节点
                    for (String node : path) {
                        writer.println("  \"" + node + "\" [fillcolor=\"" + color + "20\"];"); // 添加透明度
                    }
                    
                    // 标记路径上的边
                    for (int j = 0; j < path.size() - 1; j++) {
                        writer.println("  \"" + path.get(j) + "\" -> \"" + path.get(j + 1) + 
                                       "\" [color=\"" + color + "\", penwidth=2.0];");
                    }
                }
            }
            
            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String[] showDirectedGraph(TextToGraph graphBuilder, String filePath, String dotFilePath, String imageFilePath) throws IOException {
        System.out.println("开始生成有向图");
        String[] words = graphBuilder.buildDirectedGraph(filePath); // 构建有向图
        graphBuilder.createDotFile(dotFilePath,null,words[0]); // 生成 DOT 文件
        convertDotToImage(dotFilePath, imageFilePath); // 将 DOT 文件转换为图像文件
        return words;
    }

    // 使用 Graphviz 将 DOT 文件转换为图像文件
    public static void convertDotToImage(String dotFilePath, String imageFilePath) {
        try {
            // 创建目录确保可以写入文件
            File imageFile = new File(imageFilePath);
            if (imageFile.getParentFile() != null) {
                imageFile.getParentFile().mkdirs();
            }
            
            // 尝试使用 Graphviz
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("dot", "-Tpng", dotFilePath, "-o", imageFilePath);
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("有向图生成成功: " + imageFilePath);
                    return;
                } else {
                    System.out.println("Graphviz 执行失败，尝试使用备选方法...");
                }
            } catch (IOException e) {
                System.out.println("找不到 Graphviz，请确保已安装并添加到系统路径中");
                System.out.println("您可以从 https://graphviz.org/download/ 下载安装");
                System.out.println("DOT 文件已生成: " + dotFilePath);
                System.out.println("您可以手动使用 Graphviz 将其转换为图像");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String generateNewText(String inputText) {
        StringBuilder newText = new StringBuilder();
        String[] words = inputText.split(" ");
        Random random = new Random(); // 创建随机数生成器

        for (int i = 0; i < words.length - 1; i++) {
            String currentWord = words[i];
            String nextWord = words[i + 1];

            // 将当前单词添加到新文本中
            newText.append(currentWord).append(" ");

            // 查询当前单词和下一个单词之间的桥接词
            Set<String> bridgeWords = queryBridgeWords(currentWord, nextWord, false);
            if (bridgeWords != null && !bridgeWords.isEmpty()) {
                // 如果存在桥接词，则随机选择一个插入到新文本中
                List<String> bridgeWordsList = new ArrayList<>(bridgeWords);
                String randomBridge = bridgeWordsList.get(random.nextInt(bridgeWordsList.size()));
                newText.append(randomBridge).append(" ");
            }
        }

        // 将最后一个单词添加到新文本中
        newText.append(words[words.length - 1]);

        // 打印生成的新文本
        System.out.println("生成的新文本为: " + newText.toString());

        return newText.toString();
    }

    // 查询桥接词
    public Set<String> queryBridgeWords(String start, String end, Boolean print) {
        List<String> path = new ArrayList<>();
        if (!directedGraph.containsKey(start) && print) {
            System.out.println("在图中没有\"" + start + "\"");
            return null;
        }
        if (!directedGraph.containsKey(end) && print) {
            System.out.println("在图中没有\"" + end + "\"");
            return null;
        }
        // 广度优先搜索搜索桥接词
        Set<String> bridgeWords = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(start);
        visited.add(start);
        String current = queue.poll();
        for (Map.Entry<String, Integer> entry : directedGraph.getOrDefault(current, Collections.emptyMap()).entrySet()) {
            String neighbor = entry.getKey();
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                queue.offer(neighbor);
                if (directedGraph.containsKey(neighbor) && directedGraph.get(neighbor).containsKey(end)) {
                    bridgeWords.add(neighbor);
                }
            }
        }
        
        // 修改输出格式
        if (print) {
            if (bridgeWords.isEmpty()) {
                System.out.println(start + "和" + end + "之间没有桥接词");
            } else {
                // 将桥接词转换为列表以便格式化输出
                List<String> bridgeWordsList = new ArrayList<>(bridgeWords);
                StringBuilder output = new StringBuilder("The bridge words from " + start + " to " + end + " are: ");
                
                if (bridgeWordsList.size() == 1) {
                    output.append(bridgeWordsList.get(0)).append(".");
                } else {
                    for (int i = 0; i < bridgeWordsList.size() - 1; i++) {
                        output.append(bridgeWordsList.get(i));
                        if (i < bridgeWordsList.size() - 2) {
                            output.append(", ");
                        } else {
                            output.append(" and ");
                        }
                    }
                    output.append(bridgeWordsList.get(bridgeWordsList.size() - 1)).append(".");
                }
                
                System.out.println(output.toString());
            }
        }
        
        return bridgeWords;
    }

       // 使用改进的Dijkstra算法找出所有最短路径，考虑边的权值
       private List<List<Object>> findAllShortestPaths(String start, String end) {
           // 如果起点或终点不存在于图中，返回空列表
           if (!directedGraph.containsKey(start) || !directedGraph.containsKey(end)) {
               return new ArrayList<>();
           }
           
           // 初始化距离表
           Map<String, Integer> distance = new HashMap<>();
           // 存储每个节点的所有前驱节点及对应的边权重
           Map<String, Map<String, Integer>> predecessors = new HashMap<>();
           
           // 初始化所有节点的距离为无穷大
           for (String node : directedGraph.keySet()) {
               distance.put(node, Integer.MAX_VALUE);
               predecessors.put(node, new HashMap<>());
           }
           
           // 使用优先队列进行Dijkstra算法
           PriorityQueue<Map.Entry<String, Integer>> queue = new PriorityQueue<>(
               Comparator.comparingInt(Map.Entry::getValue)
           );
           distance.put(start, 0);
           queue.offer(new AbstractMap.SimpleEntry<>(start, 0));
           
           // Dijkstra算法找到最短距离
           while (!queue.isEmpty()) {
               Map.Entry<String, Integer> current = queue.poll();
               String currentNode = current.getKey();
               int currentDistance = current.getValue();
               
               // 如果当前距离大于已知距离，跳过（可能是队列中的旧条目）
               if (currentDistance > distance.get(currentNode)) {
                   continue;
               }
               
               // 遍历当前节点的所有邻居
               for (Map.Entry<String, Integer> neighbor : directedGraph.getOrDefault(currentNode, Collections.emptyMap()).entrySet()) {
                   String nextNode = neighbor.getKey();
                   int weight = neighbor.getValue(); // 使用实际的边权重
                   int newDistance = currentDistance + weight;
                   
                   // 如果找到更短的路径
                   if (newDistance < distance.get(nextNode)) {
                       distance.put(nextNode, newDistance);
                       predecessors.get(nextNode).clear();
                       predecessors.get(nextNode).put(currentNode, weight);
                       queue.offer(new AbstractMap.SimpleEntry<>(nextNode, newDistance));
                   } 
                   // 如果找到等长的路径
                   else if (newDistance == distance.get(nextNode)) {
                       predecessors.get(nextNode).put(currentNode, weight);
                   }
               }
           }
           
           // 如果没有找到路径到终点
           if (distance.get(end) == Integer.MAX_VALUE) {
               return new ArrayList<>();
           }
           
           // 使用DFS重建所有最短路径
           List<List<Object>> allPaths = new ArrayList<>();
           List<String> currentPath = new ArrayList<>();
           currentPath.add(end);
           
           buildAllPaths(start, end, predecessors, currentPath, allPaths, distance.get(end));
           
           return allPaths;
       }
       
       // 使用DFS重建所有最短路径
       private void buildAllPaths(String start, String current, Map<String, Map<String, Integer>> predecessors, 
                                 List<String> currentPath, List<List<Object>> allPaths, int pathLength) {
           if (current.equals(start)) {
               // 找到一条完整路径，需要反转路径
               List<String> completePath = new ArrayList<>(currentPath);
               Collections.reverse(completePath);
               
               List<Object> resultPath = new ArrayList<>();
               resultPath.add(completePath);
               resultPath.add(pathLength);
               
               allPaths.add(resultPath);
               return;
           }
           
           // 遍历所有前驱节点
           for (Map.Entry<String, Integer> predecessor : predecessors.get(current).entrySet()) {
               String predecessorNode = predecessor.getKey();
               currentPath.add(predecessorNode);
               buildAllPaths(start, predecessorNode, predecessors, currentPath, allPaths, pathLength);
               currentPath.remove(currentPath.size() - 1); // 回溯
           }
       }

       // 计算两个单词之间的所有最短路径，并将它们标记为不同颜色
       public List<List<Object>> calcShortestPath(String word1, String word2, int i, String root) {
           List<List<Object>> shortestPaths = findAllShortestPaths(word1, word2);
           
           if (shortestPaths.isEmpty()) {
               System.out.println("No path found between \"" + word1 + "\" and \"" + word2 + "\"");
               return null;
           }
           
           System.out.println("找到 " + shortestPaths.size() + " 条从 \"" + word1 + "\" 到 \"" + word2 + "\" 的最短路径：");
           for (int j = 0; j < shortestPaths.size(); j++) {
               List<Object> p = shortestPaths.get(j);
               List<String> path = (List<String>) p.get(0);
               int length = (int) p.get(1);
               System.out.println("路径 " + (j+1) + ": " + String.join(" -> ", path) + ", 总权重: " + length);
           }
           
           // 生成文件后缀加上i
           String dotFilePath = "./graph/directed_graph_shortest" + i + ".dot";
           String imageFilePath = "./graph/directed_graph_shortest" + i + ".png";
           
           // 生成 DOT 文件并标记最短路径为不同颜色
           createDotFile(dotFilePath, shortestPaths, root);
           
           // 转换 DOT 文件为图像文件
           convertDotToImage(dotFilePath, imageFilePath);
           
           return shortestPaths;
       }

    // 计算单词的PageRank值
    public Double calPageRank(String word) {
        if (!directedGraph.containsKey(word)) {
            System.out.println("在图中没有\"" + word + "\"");
            return 0.0;
        }

        // 初始化PageRank值
        Map<String, Double> pageRank = new HashMap<>();
        Map<String, Double> newPageRank = new HashMap<>();
        
        // 初始化所有节点的PageRank值为1.0
        for (String node : directedGraph.keySet()) {
            pageRank.put(node, 1.0);
        }
        
        // 阻尼系数d设为0.85
        double d = 0.85;
        double epsilon = 0.0001; // 收敛阈值
        int maxIterations = 100; // 最大迭代次数
        
        // 迭代计算PageRank
        for (int iter = 0; iter < maxIterations; iter++) {
            double diff = 0.0;
            
            // 计算每个节点的新PageRank值
            for (String node : directedGraph.keySet()) {
                double sum = 0.0;
                
                // 查找所有指向当前节点的节点
                for (String sourceNode : directedGraph.keySet()) {
                    if (directedGraph.get(sourceNode).containsKey(node)) {
                        // 计算sourceNode对node的贡献
                        int weight = directedGraph.get(sourceNode).get(node);
                        int totalWeight = 0;
                        
                        // 计算sourceNode的出边总权重
                        for (int outWeight : directedGraph.get(sourceNode).values()) {
                            totalWeight += outWeight;
                        }
                        
                        // 加权贡献
                        sum += pageRank.get(sourceNode) * ((double) weight / totalWeight);
                    }
                }
                
                // 计算新的PageRank值
                double newRank = (1 - d) + d * sum;
                newPageRank.put(node, newRank);
                
                // 计算与上一次迭代的差异
                diff += Math.abs(newRank - pageRank.get(node));
            }
            
            // 更新PageRank值
            pageRank = new HashMap<>(newPageRank);
            
            // 如果收敛，则提前结束迭代
            if (diff < epsilon) {
                System.out.println("PageRank算法在第" + (iter + 1) + "次迭代后收敛");
                break;
            }
        }
        
        // 返回指定单词的PageRank值
        return pageRank.get(word);
    }

    public String randomWalk() {
        // 选择随机起点
        Random random = new Random();
        List<String> vertices = new ArrayList<>(directedGraph.keySet());
        String currentVertex = vertices.get(random.nextInt(vertices.size()));

        StringBuilder randomWalkText = new StringBuilder();
        // 记录遍历的节点和边
        List<String> visitedVertices = new ArrayList<>();
        List<String> visitedEdges = new ArrayList<>();

        // 开始随机游走
        while (!stopWalk) {
            visitedVertices.add(currentVertex);
            Map<String, Integer> edges = directedGraph.get(currentVertex);
            if (edges != null && !edges.isEmpty()) {
                List<String> nextVertices = new ArrayList<>(edges.keySet());
                String nextVertex = nextVertices.get(random.nextInt(nextVertices.size()));
                String edge = currentVertex + " -> " + nextVertex;
                visitedEdges.add(edge);
                currentVertex = nextVertex;

                // 模拟延迟以便于演示
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                break; // 当前节点没有出边，结束随机游走
            }
        }

        // 将遍历的节点输出为文本，并以文件形式写入磁盘
        String outputPath = "./random_walk.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("随机游走的节点:");
            for (String vertex : visitedVertices) {
                randomWalkText.append(vertex).append(" ");
            }
            writer.println(randomWalkText);

            System.out.println("随机游走的结果已写入文件：" + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputPath;
    }
}