public class Main {
    public static void main(String[] args) {
        Memory m = new Memory();
        m.loadROM("demo.ch8");
        System.out.println(String.format("%04x", (int)m.getOpcode((char)0x200)));
        m.printMemory();
    }
}