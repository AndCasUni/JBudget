package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.controller.AddController;
import it.unicam.cs.mpgc.jbudget125637.controller.ConfrontoController;
import it.unicam.cs.mpgc.jbudget125637.controller.RevisionController;
import javafx.application.Platform;

    /**
     * Servizio per aggiornare le viste delle tab "Aggiungi" e "Confronto".
     * Permette di registrare i controller delle tab e di richiamare i metodi
     * di aggiornamento delle rispettive viste.
     */
    public class RefreshService {
        private static AddController addController;
    private static ConfrontoController confrontoController;
    private static RevisionController revisioneController;


    public static void registerAddController(AddController controller) {
            addController = controller;
        }

        public static void registerConfrontoController(ConfrontoController controller) {
            confrontoController = controller;
        }
        public static void registerRevisioneController(RevisionController controller) {
            revisioneController = controller;
        }

        public static void refreshAddTab() {
            if (addController != null) {
                Platform.runLater(() -> {
                    addController.refreshRecentOperationsList();
                });
            }
        }

    public static void refreshConfrontoTab() {
        if (confrontoController != null) {
            Platform.runLater(() -> {
                confrontoController.initialize();
            });
        }
    }
        public static void refreshRevisioneTab() {
            if (revisioneController != null) {
                Platform.runLater(() -> {
                    revisioneController.clearFilters();
                });
            }
        }
        public static void refreshAll() {
            refreshAddTab();
            refreshConfrontoTab();
            refreshRevisioneTab();
        }
    }
