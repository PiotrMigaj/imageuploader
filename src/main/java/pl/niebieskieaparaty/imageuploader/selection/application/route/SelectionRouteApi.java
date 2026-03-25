package pl.niebieskieaparaty.imageuploader.selection.application.route;

public interface SelectionRouteApi {
    String DIRECT_GET_ALL_BLOCKED_SELECTIONS = "direct:getAllBlockedSelections";
    String DIRECT_GET_ALL_NOT_BLOCKED_SELECTIONS = "direct:getAllNotBlockedSelections";
    String DIRECT_PROCESS_SELECTION = "direct:processSelection";
    String DIRECT_GET_SELECTED_IMAGE_NAMES = "direct:getSelectedImageNames";
}
