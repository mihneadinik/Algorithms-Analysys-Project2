import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Retele extends Task {
    public int N;
    public int M;
    public int K;
    public List<List<Integer>> friends;
    public String answer;

    public Retele() {
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
        return i * this.N + (j + 1);
    }

    private String appendNumbers(int nr1, int nr2) {
        return -nr1 + " " + -nr2 + " 0\n";
    }

    @Override
    public void formulateOracleQuestion() throws IOException {
        StringBuilder toWrite = new StringBuilder();
        int nrVars = this.N * this.K;
        int nrClauses = 0;

        // a - pentru fiecare pozitie din grup exista cel putin un nod
        for (int i = 0; i < this.K; i++) {
            for (int j = 0; j < this.N; j++) {
                toWrite.append(createNumber(i, j));
                toWrite.append(" ");
            }
            toWrite.append("0\n");
            nrClauses++;
        }

        // b - 2 noduri care nu formeaza muchie nu pot aparea simultan in grup
        for (int i = 0; i < this.K; i++) {
            for (int j = 0; j < this.K; j++) {
                if (i != j) {
                    for (int v = 0; v < this.N; v++) {
                        // graful fiind neorientat nu e nevoie sa verificam si pentru inversul perechii
                        for (int w = v + 1; w < this.N; w++) {
                            if (!this.friends.get(v).contains(w + 1)) {
                                // nu e muchie
                                toWrite.append(appendNumbers(createNumber(i, v), createNumber(j, w)));
                                nrClauses++;
                            }
                        }
                    }
                }
            }
        }

        // c - 2 noduri diferite nu pot ocupa aceeasi pozitie + un nod nu poate ocupa 2 pozitii
        for (int i = 0; i < this.K; i++) {
            for (int j = i + 1; j < this.K; j++) {
                for (int v = 0; v < this.N; v++) {
                    toWrite.append(appendNumbers(createNumber(i, v), createNumber(j, v)));
                    nrClauses++;
                }
            }
        }
        for (int i = 0; i < this.K; i++) {
            for (int v = 0; v < this.N; v++) {
                for (int w = v + 1; w < this.N; w++) {
                    toWrite.append(appendNumbers(createNumber(i, v), createNumber(i, w)));
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
            StringBuilder response = new StringBuilder();
            response.append("True\n");
            int V = sc.nextInt();
            for (int i = 0; i < V; i++) {
                // caut valorile care au intors True (nu sunt negative)
                int read = sc.nextInt();
                if (read > 0) {
                    response.append(read);
                    response.append(" ");
                }
            }

            // sterg ultimul spatiu
            response.deleteCharAt(response.toString().length() - 1);
            this.answer = response.toString();
        }
    }

    @Override
    public void writeAnswer() throws IOException {
        System.out.println(this.answer);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Retele task = new Retele();
        task.solve();
    }
}