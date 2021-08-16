package application.Model.Components;

public interface LineComponent extends Component {
    Edge getEdge();

    @Override
    default void update() {
        getEdge().updateSegment();
    }
}
