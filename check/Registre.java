import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Registre extends Task{
    public int N;
    public int M;
    public int K;
    public List<List<Integer>> friends;
    public String answer;

    public Registre() {
        this.N = 0;
        this.M = 0;
        this.K = 0;
        this.friends = new ArrayList<>();
        this.answer = "";
    }

    @Override
    public void solve() throws IOException, InterruptedException {
        // citim inputul
        readProblemData();
        // pregatim cererea
        formulateOracleQuestion();
        askOracle();
        // citim raspunsul
        decipherOracleAnswer();
        // afisam
        writeAnswer();
    }

    @Override
    public void readProblemData() throws IOException {
        // citim de la stdin
        Scanner in = new Scanner(System.in);
        this.N = in.nextInt();
        this.M = in.nextInt();
        this.K = in.nextInt();

        // creez o lista de vecini
        for (int i = 0; i < this.N; i++) {
            this.friends.add(new ArrayList<Integer>());
        }

        // citim relatiile de prietenie
        for (int i = 0; i < this.M; i++) {
            int node1 = in.nextInt();
            int node2 = in.nextInt();
            this.friends.get(node1 - 1).add(node2);
        }
    }

    private int createNumber(int i, int j) {
        // asigneaza un numar fiecarei variabile in functie de indicii ei
        return i * this.K + (j + 1);
    }

    private String appendNumbers(int nr1, int nr2) {
        return -nr1 + " " + -nr2 + " 0\n";
    }

    @Override
    public void formulateOracleQuestion() throws IOException {
        StringBuilder toWrite = new StringBuilder();
        int nrVars = this.N * this.K;
        int nrClauses = 0;

        // fiecare nod sta intr-un registru
        for (int i = 0; i < this.N; i++) {
            for (int j = 0; j < this.K; j++) {
                toWrite.append(createNumber(i, j));
                toWrite.append(" ");
            }
            toWrite.append("0\n");
            nrClauses++;
        }

        // nodurile unei muchii nu pot fi in acelasi reg
        for (int u = 0; u < this.friends.size(); u++) {
            for (int v = 0; v < this.friends.get(u).size(); v++) {
                for (int i = 0; i < this.K; i++) {
                    toWrite.append(appendNumbers(createNumber(u, i), createNumber(this.friends.get(u).get(v) - 1, i)));
                    nrClauses++;
                }
            }
        }

        // un nod nu poate fi in 2 reg
        for (int v = 0; v < this.N; v++) {
            for (int i = 0; i < this.K; i++) {
                for (int j = i + 1; j < this.K; j++) {
                    toWrite.append(appendNumbers(createNumber(v, i), createNumber(v, j)));
                    nrClauses++;
                }
            }
        }

        // cream formatul fisierului pentru oracol
        StringBuilder finalFile = new StringBuilder();
        finalFile.append("p cnf ");
        finalFile.append(nrVars);
        finalFile.append(" ");
        finalFile.append(nrClauses);
        finalFile.append('\n');
        finalFile.append(toWrite.toString());

        // scriem in fisier
        BufferedWriter out = new BufferedWriter(new FileWriter("./sat.cnf"));
        try {
            out.write(finalFile.toString());
        } catch(IOException e1) {
            System.out.println("Error in writing to file");
        } finally {
            out.close();
        }
    }

    @Override
    public void decipherOracleAnswer() throws IOException {
        // citim ce a intors oracolul
        Scanner sc = new Scanner(new File("./sat.sol"));

        if (Objects.equals(sc.nextLine(), "False")) {
            this.answer = "False";
        } else {
            // daca problema are solutie
            this.answer = "True\n";
            StringBuilder response = new StringBuilder();
            int V = sc.nextInt();
            int[] nodes = new int[this.N];
            for (int i = 0; i < V; i++) {
                // caut valorile care au intors True (nu sunt negative)
                int read = sc.nextInt();
                if (read > 0) {
                    int index = read / this.K;
                    read %= this.K;
                    if (read != 0) {
                        nodes[index] = read;
                    } else {
                        nodes[index - 1] = this.K;
                    }
                }
            }
            for (int reg : nodes) {
                response.append(reg);
                response.append(" ");
            }

            // sterg ultimul spatiu
            response.deleteCharAt(response.toString().length() - 1);
            this.answer += response.toString();
        }
    }

    @Override
    public void writeAnswer() throws IOException {
        System.out.println(this.answer);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Registre task = new Registre();
        task.solve();
    }
}