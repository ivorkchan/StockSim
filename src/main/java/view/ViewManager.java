package view;

import view.view_events.SwitchPanelEvent;
import view.view_events.ViewEvent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ViewManager {
    private static ViewManager instance;
    private final List<IComponent> components = new ArrayList<>();
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private ViewManager() {
    }

    public static ViewManager Instance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }

    public void setCardLayout(CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
    }

    public void registerComponent(IComponent component) {
        components.add(component);
    }

    public void broadcastEvent(ViewEvent event) {
        if (event instanceof SwitchPanelEvent) {
            switchPanel(((SwitchPanelEvent) event).getPanelName());
        }

        components.stream()
                .filter(component -> event.getTypes().stream().anyMatch(component::supportsEvent))
                .forEach(component -> {
                    System.out.println("Broadcasting " + event.getClass().getSimpleName() + " to " + component.getClass().getSimpleName());
                    component.receiveViewEvent(event);
                });
    }

    public void switchPanel(String panelName) {
        if (cardLayout == null || cardPanel == null) {
            throw new IllegalStateException("CardLayout or CardPanel is not set up in ViewManager.");
        }
        cardLayout.show(cardPanel, panelName);
        System.out.println("Switched to panel: " + panelName);
    }
}