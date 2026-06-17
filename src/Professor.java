public class Professor extends Servidor implements IAutenticar{

    private int matricula;
    private String nome;

    public Professor(int matricula, String nome){
        this.matricula = matricula;
        this.nome = nome;
    }

    public String getNome(){
        return this.nome;
    }

    public int getMatricula(){
        return this.matricula;
    }

    public boolean login(Servidor s){
        if(s.getMatricula() == 12345){
            return true;
        }
        return false;
    }
}
