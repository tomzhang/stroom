package stroom.explorer.impl;

import io.swagger.annotations.Api;
import stroom.docref.DocRef;
import stroom.docref.DocRefInfo;
import stroom.explorer.api.ExplorerService;
import stroom.explorer.shared.BulkActionResult;
import stroom.explorer.shared.ExplorerNode;
import stroom.explorer.shared.ExplorerTreeFilter;
import stroom.explorer.shared.HasNodeState;
import stroom.explorer.shared.PermissionInheritance;
import stroom.security.api.SecurityContext;
import stroom.security.shared.DocumentPermissionNames;
import stroom.util.shared.RestResource;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Api(value = "explorer - /v1")
@Path("/explorer/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExplorerResource implements RestResource {
    private final ExplorerService explorerService;
    private final ExplorerTreeModel explorerTreeModel;
    private final SecurityContext securityContext;

    @Inject
    public ExplorerResource(final ExplorerService explorerService,
                            final ExplorerTreeModel explorerTreeModel,
                            final SecurityContext securityContext) {
        this.explorerService = explorerService;
        this.explorerTreeModel = explorerTreeModel;
        this.securityContext = securityContext;
    }

    @GET
    @Path("/search")
    public Response search(
            final @QueryParam("searchTerm") String searchTerm,
            final @QueryParam("pageOffset") Long pageOffset,
            final @QueryParam("pageSize") Long pageSize) {
        // For now, just do this every time the whole tree is fetched
        explorerTreeModel.rebuild();

        final TreeModel treeModel = explorerTreeModel.getModel();
        final TreeModel filteredModel = new TreeModel();

        final ExplorerTreeFilter filter = new ExplorerTreeFilter(
                null,
                null,
                Collections.singleton(DocumentPermissionNames.READ),
                searchTerm,
                true);

        filterDescendants(null, treeModel, filteredModel, 0, filter);

        // Flatten this tree out
        final List<DocRef> results = new ArrayList<>();
        getFlatResults(filteredModel, results);

        return Response.ok(results).build();
    }

    @GET
    @Path("/all")
    public Response getExplorerTree() {
        // For now, just do this every time the whole tree is fetched
        explorerTreeModel.rebuild();

        final TreeModel treeModel = explorerTreeModel.getModel();
        final TreeModel filteredModel = new TreeModel();

        final ExplorerTreeFilter filter = new ExplorerTreeFilter(
                null,
                null,
                Collections.singleton(DocumentPermissionNames.READ),
                null,
                true);

        filterDescendants(null, treeModel, filteredModel, 0, filter);
        final SimpleDocRefTreeDTO result = getRoot(filteredModel);

        return Response.ok(result).build();
    }

    @GET
    @Path("/info/{type}/{uuid}")
    public Response getDocInfo(@PathParam("type") final String type,
                               @PathParam("uuid") final String uuid) {
        final DocRefInfo info = explorerService.info(new DocRef.Builder()
                .type(type)
                .uuid(uuid)
                .build());

        return Response.ok(info).build();
    }

    /**
     * @return The DocRef types currently used in this tree.
     */
    @GET
    @Path("/docRefTypes")
    public Response getDocRefTypes() {
        explorerTreeModel.rebuild();
        final TreeModel treeModel = explorerTreeModel.getModel();

        List<String> docRefTypes = treeModel.getChildMap().values().stream()
                .flatMap(List::stream)
                .map(elementNode -> elementNode == null ? "" : elementNode.getType())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return Response.ok(docRefTypes).build();
    }

    static class CreateOp {
        private String docRefType;
        private String docRefName;
        private DocRef destinationFolderRef;
        private PermissionInheritance permissionInheritance;

        public String getDocRefType() {
            return docRefType;
        }

        public void setDocRefType(String docRefType) {
            this.docRefType = docRefType;
        }

        public String getDocRefName() {
            return docRefName;
        }

        public void setDocRefName(String docRefName) {
            this.docRefName = docRefName;
        }

        public DocRef getDestinationFolderRef() {
            return destinationFolderRef;
        }

        public void setDestinationFolderRef(DocRef destinationFolderRef) {
            this.destinationFolderRef = destinationFolderRef;
        }

        public PermissionInheritance getPermissionInheritance() {
            return permissionInheritance;
        }

        public void setPermissionInheritance(PermissionInheritance permissionInheritance) {
            this.permissionInheritance = permissionInheritance;
        }
    }

    @POST
    @Path("/create")
    public Response createDocument(final CreateOp op) {
        explorerService.create(op.docRefType, op.docRefName, op.destinationFolderRef, op.permissionInheritance);

        return getExplorerTree();
    }

    static class CopyOp {
        private List<DocRef> docRefs;
        private DocRef destinationFolderRef;
        private PermissionInheritance permissionInheritance;

        public List<DocRef> getDocRefs() {
            return docRefs;
        }

        public void setDocRefs(List<DocRef> docRefs) {
            this.docRefs = docRefs;
        }

        public DocRef getDestinationFolderRef() {
            return destinationFolderRef;
        }

        public void setDestinationFolderRef(DocRef destinationFolderRef) {
            this.destinationFolderRef = destinationFolderRef;
        }

        public PermissionInheritance getPermissionInheritance() {
            return permissionInheritance;
        }

        public void setPermissionInheritance(PermissionInheritance permissionInheritance) {
            this.permissionInheritance = permissionInheritance;
        }
    }

    @POST
    @Path("/copy")
    public Response copyDocument(final CopyOp op) {
        final BulkActionResult result = explorerService.copy(op.docRefs, op.destinationFolderRef, op.permissionInheritance);
        if (result.getMessage().isEmpty()) {
            return getExplorerTree();
        } else {
            return Response.serverError().entity(result.getMessage()).build();
        }
    }

    static class MoveOp {
        private List<DocRef> docRefs;
        private DocRef destinationFolderRef;
        private PermissionInheritance permissionInheritance;

        public List<DocRef> getDocRefs() {
            return docRefs;
        }

        public void setDocRefs(List<DocRef> docRefs) {
            this.docRefs = docRefs;
        }

        public DocRef getDestinationFolderRef() {
            return destinationFolderRef;
        }

        public void setDestinationFolderRef(DocRef destinationFolderRef) {
            this.destinationFolderRef = destinationFolderRef;
        }

        public PermissionInheritance getPermissionInheritance() {
            return permissionInheritance;
        }

        public void setPermissionInheritance(PermissionInheritance permissionInheritance) {
            this.permissionInheritance = permissionInheritance;
        }
    }

    @PUT
    @Path("/move")
    public Response moveDocument(final MoveOp op) {
        final BulkActionResult result = explorerService.move(op.docRefs, op.destinationFolderRef, op.permissionInheritance);
        if (result.getMessage().isEmpty()) {
            return getExplorerTree();
        } else {
            return Response.serverError().entity(result.getMessage()).build();
        }
    }

    static class RenameOp {
        private DocRef docRef;
        private String name;

        public DocRef getDocRef() {
            return docRef;
        }

        public void setDocRef(DocRef docRef) {
            this.docRef = docRef;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @PUT
    @Path("/rename")
    public Response renameDocument(final RenameOp renameOp) {
        final DocRef result = explorerService.rename(renameOp.docRef, renameOp.name);

        return getExplorerTree();
    }

    @DELETE
    @Path("/delete")
    public Response deleteDocument(final List<DocRef> docRefs) {
        final BulkActionResult result = explorerService.delete(docRefs);
        if (result.getMessage().isEmpty()) {
            return getExplorerTree();
        } else {
            return Response.serverError().entity(result.getMessage()).build();
        }
    }

    private boolean filterDescendants(final ExplorerNode parent,
                                      final TreeModel treeModelIn,
                                      final TreeModel treeModelOut,
                                      final int currentDepth,
                                      final ExplorerTreeFilter filter) {
        int added = 0;

        final List<ExplorerNode> children = treeModelIn.getChildMap().get(parent);
        if (children != null) {

            for (final ExplorerNode child : children) {
                // Recurse right down to find out if a descendant is being added and therefore if we need to include this as an ancestor.
                final boolean hasChildren = filterDescendants(child, treeModelIn, treeModelOut, currentDepth + 1, filter);
                if (hasChildren) {
                    treeModelOut.add(parent, child);
                    added++;

                } else if (ExplorerServiceImpl.checkType(child, filter.getIncludedTypes())
                        && ExplorerServiceImpl.checkTags(child, filter.getTags())
                        && (ExplorerServiceImpl.checkName(child, filter.getNameFilter()))
                        && checkSecurity(child, filter.getRequiredPermissions())) {
                    treeModelOut.add(parent, child);
                    added++;
                }
            }
        }

        return (added > 0);
    }

    private boolean checkSecurity(final ExplorerNode explorerNode, final Set<String> requiredPermissions) {
        if (requiredPermissions == null || requiredPermissions.size() == 0) {
            return false;
        }

        final String type = explorerNode.getType();
        final String uuid = explorerNode.getDocRef().getUuid();
        for (final String permission : requiredPermissions) {
            if (!securityContext.hasDocumentPermission(type, uuid, permission)) {
                return false;
            }
        }

        return true;
    }

    private void getFlatResults(final TreeModel filteredModel, final List<DocRef> results) {
        final List<ExplorerNode> children = filteredModel.getChildMap().get(null);
        if (children != null) {
            final ExplorerNode systemNode = children.stream()
                    .filter(c -> c.getName().equals("System"))
                    .findFirst()
                    .orElse(children.get(0));

            results.add(new DocRef.Builder()
                    .uuid(systemNode.getUuid())
                    .name(systemNode.getName())
                    .type(systemNode.getType())
                    .build());

            getChildren(systemNode, filteredModel)
                    .stream()
                    .map(c ->
                            new DocRef.Builder()
                                    .uuid(c.getUuid())
                                    .name(c.getName())
                                    .type(c.getType())
                                    .build())
                    .forEach(results::add);
        }
    }

    private SimpleDocRefTreeDTO getRoot(final TreeModel filteredModel) {
        SimpleDocRefTreeDTO result = null;

        final List<ExplorerNode> children = filteredModel.getChildMap().get(null);
        if (children != null) {
            final ExplorerNode systemNode = children.stream()
                    .filter(c -> c.getName().equals("System"))
                    .findFirst()
                    .orElse(children.get(0));

            result = new SimpleDocRefTreeDTO(systemNode.getUuid(), systemNode.getType(), systemNode.getName());
            getChildren(systemNode, filteredModel).forEach(result::addChild);
        }

        return result;
    }

    private List<SimpleDocRefTreeDTO> getChildren(final ExplorerNode parent,
                                                  final TreeModel filteredModel) {
        List<SimpleDocRefTreeDTO> result = new ArrayList<>();

        final List<ExplorerNode> children = filteredModel.getChildMap().get(parent);

        if (children == null) {
            parent.setNodeState(HasNodeState.NodeState.LEAF);
        } else {
            parent.setNodeState(HasNodeState.NodeState.OPEN);
            for (final ExplorerNode child : children) {
                final SimpleDocRefTreeDTO resultChild = new SimpleDocRefTreeDTO(child.getUuid(), child.getType(), child.getName());
                getChildren(child, filteredModel).forEach(resultChild::addChild);
                result.add(resultChild);
            }
        }

        return result;
    }
}
