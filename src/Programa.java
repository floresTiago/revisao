import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Programa extends JFrame {

    public Programa(){
        Professor p1 = new Professor(123, "Sla");
        System.out.println(p1.getMatricula());

        Pessoa p = p1;
        Servidor s = p1;

        if(login(p1, s)){
            System.out.println("login sucesso");
        }else{
            System.out.println("login erro");
        }

        setSize(640, 480);
        JButton btn = new JButton("login");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().add(btn);
        setVisible(true);
    }

    private void conectar() throws Exception{
        //registrar driver
        Class.forName("org.postgress.Driver");
        //abrir conexao
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "postgres";
        Connection conn = DriverManager.getConnection(url, user, password);
        //conn permite criar objetos e leva-los ao banco de dados
        String consulta = "SELECT * FROM usuario";
        Statement st = conn.createStatement();
        ResultSet rs =  st.executeQuery(consulta);
        rs.close();
        conn.close();
    }

    private boolean login(IAutenticar autenticador, Servidor s){
        return autenticador.login(s);
    }

    public static void main(String[] args) {
        new Programa();
    }
}
