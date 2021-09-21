/*
 * Copyright 2016 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.xinsolutions.rest.content.visitors;

import org.apache.commons.lang3.StringUtils;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.restapi.ResourceContext;
import org.hippoecm.hst.restapi.content.linking.Link;
import org.hippoecm.hst.restapi.content.visitors.HippoMirrorVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hippoecm.repository.api.HippoNodeType.HIPPO_DOCBASE;
import static org.hippoecm.repository.api.HippoNodeType.NT_MIRROR;

/**
 * Override hippo mirror visitor in rest api so we can return a folder when selected in hippo:mirror.
 */
public class XinHippoMirrorVisitor extends HippoMirrorVisitor {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XinHippoMirrorVisitor.class);

    public XinHippoMirrorVisitor() {
        LOG.info("Initialise");
    }

    /**
     * Override the visit node implementation to handle "folder" references.
     *
     * @param context
     * @param node
     * @param response
     *
     * @throws RepositoryException
     */
    @Override
    protected void visitNode(final ResourceContext context, final Node node, final Map<String, Object> response) throws RepositoryException {

        final String docbase = node.getProperty(HIPPO_DOCBASE).getString();
        if (StringUtils.isBlank(docbase) || docbase.equals("cafebabe-cafe-babe-cafe-babecafebabe")) {
            response.put("link", Link.invalid);
            return;
        }

        Node refNode = node.getSession().getNodeByIdentifier(docbase);

        boolean isHippoFolder = refNode.getPrimaryNodeType().getName().equals("hippostd:folder");
        if (isHippoFolder) {

            // prepare object with path information.
            Map<String, String> linkInfo = new LinkedHashMap<>();
            linkInfo.put("type", "path");
            linkInfo.put("id", docbase);
            linkInfo.put("path", refNode.getPath());

            response.put("link", linkInfo);

            return;
        }

        final HstRequestContext requestContext = context.getRequestContext();
        final HstLink hstLink = requestContext.getHstLinkCreator().create(docbase, requestContext.getSession(), requestContext);
        final Link link = context.getRestApiLinkCreator().convert(context, docbase, hstLink);
        response.put("link", link);

    }

    @Override
    public String getNodeType() {
        return NT_MIRROR;
    }
}