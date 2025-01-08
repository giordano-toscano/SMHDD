package smhdd.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class D {

    private String[][] examples; // equivalent to dadosStr
    private String[] columns;   // equivalent to nomeVariaveis 
    private String name; // dataset name

    private int[][] dp; // positive examples
    private int[][] dn; // negative examples

    public D(String path, String delimiter) throws IOException{
        this.examples = this.loadFile(path, delimiter);
    }

    //CHECKED !!!
    private String[][] loadFile(String filePath, String delimiter) throws IOException{    
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Column names in the first line
            line = br.readLine();
            this.columns = line.split(delimiter);

            while ((line = br.readLine()) != null) {
                // Split the line by commas (or other delimiter if necessary)
                String[] row = line.split(delimiter);
                data.add(row);
            }
        }
        // Convert List<String[]> to String[][]
        return data.toArray(String[][]::new);
    }

    // private static void dadosStrToD(String[][] dadosStr){
                
    //     //Capturando os valores distintos de cada atributo
    //     ArrayList<HashSet<String>> valoresDistintosAtributos = new ArrayList<>(); //Amazena os valores distintos de cada atributo em um linha
    //     D.numeroItens = 0;
    //     for(int i = 0; i < D.numeroAtributos; i++){
    //         HashSet<String> valoresDistintosAtributo = new HashSet<>(); //Armazena valores distintos de apenas um atributo. Criar HashSet para armezenar valores distintos de um atributo. Não admite valores repetidos!
    //         for(int j = 0; j < D.numeroExemplos; j++){
    //             valoresDistintosAtributo.add(dadosStr[j][i]); //Coleção não admite valores repetidos a baixo custo computacional.
    //         }
    //         D.numeroItens += valoresDistintosAtributo.size();
            
    //         valoresDistintosAtributos.add(valoresDistintosAtributo); //Adiciona lista de valores distintos do atributo de índice i na posição i do atributo atributosEvalores
    //     }
        
    //     //Gera 4 arrays para armazenar o universo deatributos e valores no formato original (String) e mapeado para inteiro.
    //     D.itemAtributoStr = new String[D.numeroItens];
    //     D.itemValorStr = new String[D.numeroItens];
    //     D.itemAtributo = new int[D.numeroItens];
    //     D.itemValor = new int[D.numeroItens];
    //     D.numericAttributes = new ArrayList<>();
            
    //     //Carrega arrays com universos de itens com valores reais e respectivos inteiros mapeados
    //     int[][] dadosInt = new int[D.numeroExemplos][D.numeroAtributos]; //dados no formato inteiro: mais rápido compararinteiros que strings
    //     int indiceItem = 0; //Indice vai de zero ao número de itens total
    //     for(int indiceAtributo = 0; indiceAtributo < valoresDistintosAtributos.size(); indiceAtributo++){
    //         Iterator valoresDistintosAtributoIterator = valoresDistintosAtributos.get(indiceAtributo).iterator(); //Capturando valores distintos do atributo de indice i
    //         int indiceValor = 0; //vai mapear um inteiro distinto para cada valor distinto de cada variável
            
    //         //Para cada atributo: 
    //         //Atribui inteiro para atributo e a cada valor do atributo.  
    //         //Realizar mapeamento na matriz de dados no formato inteiro
    //         while(valoresDistintosAtributoIterator.hasNext()){
    //             D.itemAtributoStr[indiceItem] = D.nomeVariaveis[indiceAtributo]; //
    //             D.itemValorStr[indiceItem] = (String)valoresDistintosAtributoIterator.next();

    //             if(isNumber(D.itemValorStr[indiceItem])){
    //                 D.numericAttributes.add(indiceAtributo);
    //             }

    //             D.itemAtributo[indiceItem] = indiceAtributo;
    //             D.itemValor[indiceItem] = indiceValor;               
                
    //             //Preenche respectivo item (atributo, Valor) na matrix dadosInt com inteiro que mapeia valor categórico da base
    //             for(int m = 0; m < D.numeroExemplos; m++){
    //                 if(dadosStr[m][indiceAtributo].equals(D.itemValorStr[indiceItem])){
    //                     dadosInt[m][indiceAtributo] = D.itemValor[indiceItem];
    //                 }
    //             }
    //             indiceValor++;
    //             indiceItem++;
    //         }     
    //     } 
        
    //     //Gera Bases de exemplos positivos (D+) e negativos (D-)
    //     D.geraDpDn(dadosStr, dadosInt);
    // }




    public String getName(){
        return this.name;
    }

    public String[][] getExamples(){
        return this.examples;
    }

    public String[] getColumns(){
        return this.columns;
    }


}

/*
 * 
 * public class CSVToArrayOptimized {
    public static String[][] readCSV(String filePath) throws IOException {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                data.add(parseLine(line));
            }
        }
        return data.toArray(new String[0][]);
    }

    // Manually parse a CSV line into fields
    private static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (char ch : line.toCharArray()) {
            if (ch == '"') {
                // Toggle the inQuotes flag if a double-quote is encountered
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                // Add the field if we hit a comma outside quotes
                fields.add(field.toString());
                field.setLength(0); // Clear the StringBuilder for the next field
            } else {
                // Append the character to the current field
                field.append(ch);
            }
        }
        // Add the last field
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }
 */
