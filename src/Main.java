import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the path for the input file: ");
        String inputPath = scanner.nextLine();

        System.out.print("Enter the path for the output file: ");
        String outputPath = scanner.nextLine();

        System.out.print("Enter a value for support: ");
        int supportThreshold = scanner.nextInt();

        List<List<String>> transactions = readDatasetFromFile(inputPath);

        try (PrintStream fileOut = new PrintStream(new FileOutputStream(outputPath))) {
            System.setOut(fileOut);

            FPTree tree = new FPTree(transactions, supportThreshold, null, -1);
            Map<String, Integer> frequentPatterns = tree.minePatterns(supportThreshold);

            List<String> outputList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : frequentPatterns.entrySet()) {
                if (entry.getKey().split(" ").length > 1) {
                    outputList.add(String.format("<%s : %d>", entry.getKey(), entry.getValue()));
                }
            }

            System.out.println("Frequent Items Are:\n\n" + String.join(", ", outputList));
            Desktop.getDesktop().open(new File(outputPath));
        } catch (IOException e) {
            System.err.println("Error: Output file not found. Please provide a valid path.");
        }
    }

    private static List<List<String>> readDatasetFromFile(String filePath) {
        List<List<String>> transactions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("[", "");
                line = line.replace("]", "");
                line = line.replace("'", "");
                line = line.replace("\"", "");
                String[] items = line.split(",");
                List<String> transaction = Arrays.asList(items);
                transactions.add(transaction);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return transactions;
    }
}