package com.teafactory;

import com.teafactory.gui.FactoryGUI;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        printHeader();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Cannot set system Look and Feel");
        }

        SwingUtilities.invokeLater(() -> {
            FactoryGUI gui = new FactoryGUI();
            gui.setVisible(true);
        });
    }

    private static void printHeader() {

        System.out.println("============================================");
        System.out.println("   TEA FACTORY SIMULATOR v1.0");
        System.out.println("   Multi-threaded production simulation");
        System.out.println("============================================");
        System.out.println();
        System.out.println("Architecture:");
        System.out.println("  - Phaser: 4 phases (SUPPLY - PROCESS - PACK - CONSUME)");
        System.out.println("  - Buffers: ReentrantLock + Condition (no synchronized!)");
        System.out.println("  - Threads: 6 (1 supplier, 1 master, 1 packer, 3 buyers)");
        System.out.println();
        System.out.println("GUI starting...");
        System.out.println();
    }
}
