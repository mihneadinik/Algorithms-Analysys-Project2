import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Reclame extends Task{
    public int N;
    public int M;
    public int K;
    public List<List<Integer>> friends;
    public String answer;

    public Reclame() {
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
        // precalculam un numar minim de noduri care se afla in acoperire
        this.K = minK();
        while (Objects.equals(this.answer, "") && this.K <= this.N) {
            // pregatim cererea
            formulateOracleQuestion();
            askOracle();
            // citim raspunsul
            decipherOracleAnswer();
            this.K++;
        }
        // afisam
        writeAnswer();
    }

    @Override
    public void readProblemData() throws IOException {
        // citim de la stdin
        Scanner in = new Scanner(System.in);
        this.N = in.nextInt();
        this.M = in.nextInt();

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

    private int minK() {
        TreeSet<Integer> nodes = new TreeSet<>();
        int count = 0;
        // copiez lista de adiacenta ca sa nu o alterez pe cea originala
        List<List<Integer>> copy = new ArrayList<>();
        for (int i = 0; i < this.N; i++) {
            List<Integer> insideCopy = new ArrayList<>();
            for (int j = 0; j < this.friends.get(i).size(); j++) {
                insideCopy.add(this.friends.get(i).get(j));
            }
            copy.add(insideCopy);
        }
        // sortez lista descrescator dupa gradele externe ale nodurilor
        copy.sort(new Comparator<List<Integer>>() {
            @Override
            public int compare(List<Integer> o1, List<Integer> o2) {
                return o2.size() - o1.size();
            }
        });
        // vrea sa numar de cate noduri am nevoie ca sa acopar toate muchiile din graf
        boolean end = true;
        while (nodes.size() < this.N && end) {
            for (int i = 0; i < this.N; i++) {
                List<Integer> currNode = copy.get(i);
                boolean ok = false;
                // pentru fiecare nod in parte, verific daca nodurile destinatie ale
                // muchiilor care pleaca din el au mai fost considerate deja
                for (int j = 0; j < currNode.size(); j++) {
                    if (!nodes.contains(currNode.get(j))) {
                        nodes.add(currNode.get(j));
                        ok = true;
                    }
                }
                // daca exista cel putin un nod destinatie nou, numar nodul sursa
                if (ok) {
                    count++;
                }
            }
            // daca am ajuns la finalul listei de adiacenta si au ramas noduri izolate in graf
            end = false;
        }
        return count;
    }

    private int createNumber(int i, int j) {
        // asigneaza un numar fiecarei variabile in functie de indicii ei
        return j * this.N + (i + 1);
    }

    private String appendNumbers(int nr1, int nr2) {
        return -nr1 + " " + -nr2 + " 0\n";
    }

    @Override
    public void formulateOracleQuestion() throws IOException {
        StringBuilder toWrite = new StringBuilder();
        int nrVars = this.N * this.K;
        int nrClauses = 0;

        // exista macar un nod pt fiecare elem al acoperirii
        for (int r = 0; r < this.K; r++) {
            for (int i = 0; i < this.N; i++) {
                toWrite.append(createNumber(i, r) + " ");
            }
            toWrite.append("0\n");
            nrClauses++;
        }

        // toate muchiile au cel putin un capat in acoperire
        for (int u = 0; u < this.friends.size(); u++) {
            for (int v = 0; v < this.friends.get(u).size(); v++) {
                for (int i = 0; i < this.K; i++) {
                    toWrite.append(createNumber(u, i) + " ");
                }
                for (int i = 0; i < this.K; i++) {
                    toWrite.append(createNumber(this.friends.get(u).get(v) - 1, i) + " ");
                }
                toWrite.append("0\n");
                nrClauses++;
            }
        }

        // nu ocupa aceeasi pozitie in acoperire si un nod nu ocupa 2 pozitii
        for (int i = 0; i < this.K; i++) {
            for (int j = i + 1; j < this.K; j++) {
                for (int v = 0; v < this.N; v++) {
                    toWrite.append(appendNumbers(createNumber(v, i), createNumber(v, j)));
                    nrClauses++;
                }
            }
        }
        for (int v = 0; v < this.N; v++) {
            for (int w = v + 1; w < this.N; w++) {
                for (int i = 0; i < this.K; i++) {
                    toWrite.append(appendNumbers(createNumber(v, i), createNumber(w, i)));
                    nrClauses++;
                }
            }
        }

        // cream formatul fisierului pentru oracol
        StringBuilder finalFile = new StringBuilder();
        finalFile.append("p cnf ");
        finalFile.append(nrVars + " " + nrClauses + "\n");
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

        if (!Objects.equals(sc.nextLine(), "False")) {
            // daca problema are solutie
            StringBuilder response = new StringBuilder();
            int V = sc.nextInt();
            for (int i = 0; i < V; i++) {
                // caut valorile care au intors True (nu sunt negative)
                int read = sc.nextInt();
                if (read > 0) {
                    // scriu indicele nodului din graf
                    read %= this.N;
                    if (read != 0) {
                        response.append(read);
                    } else {
                        response.append(this.N);
                    }
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
        Reclame task = new Reclame();
        task.solve();
    }
}