﻿using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Text;
using Tridion.ExternalContentLibrary.V2;

namespace SDL.ECommerce.Ecl
{

    public abstract class SelectableItem : IContentLibraryMultimediaItem 
    {
        protected readonly IEclUri id;
        protected string externalId;
        protected string title;

        protected SelectableItem(int publicationId, string externalId, string title)
        {
            this.id = EclProvider.HostServices.CreateEclUri(publicationId, EclProvider.MountPointId,
                     externalId, DisplayTypeId, EclItemTypes.File);
            this.externalId = externalId;
            this.title = title;
        }


        public virtual bool CanGetUploadMultimediaItemsUrl
        {
            get { return false; }
        }

        public bool CanSearch
        {
            get { return false; }
        }

        public virtual string IconIdentifier
        {
            get { return null; }
        }

        public IEclUri Id
        {
            get { return id; }
        }

        public virtual string Title
        {
            get { return this.title; }
            set { throw new NotSupportedException(); }
        }

        public virtual bool IsThumbnailAvailable
        {
            get { return false; } 
        }

        public virtual DateTime? Modified
        {
            get { return null; }
        }

        public virtual string ThumbnailETag
        {
            get { return null; }
        }

        public virtual string Filename
        {
            get { return this.externalId; }
        }

        public virtual string GetTemplateFragment(IList<ITemplateAttribute> attributes)
        {
            return "";
        }

        public virtual int? Height
        {
            get { return null; }
        }

        public virtual string MimeType
        {
            get
            {
                return null;
            }
        }

        public virtual int? Width
        {
            get { return null; }
        }

        public virtual bool CanGetViewItemUrl
        {
            get { return true; }
        }

        public virtual bool CanUpdateMetadataXml
        {
            get { return false; }
        }

        public virtual bool CanUpdateTitle
        {
            get { return false; }
        }

        public virtual DateTime? Created
        {
            get { return null; }
        }

        public virtual string CreatedBy
        {
            get { return null; }
        }

        // Should be overriden by sub class when for example outputting a product image
        public virtual IContentResult GetContent(IList<ITemplateAttribute> attributes)
        {
            return null;
        }

        public string MetadataXml
        {
            get
            {
                Dictionary<string, object> metadata = new Dictionary<string, object>();
                metadata.Add("Id", this.externalId);
                metadata.Add("Name", this.title);
                this.GetMetadata(metadata);

                StringBuilder metadataXml = new StringBuilder();
                metadataXml.Append("<Metadata xmlns=\"" + EclProvider.EcommerceEclNs.NamespaceName + "\">");
                foreach (var metadataName in metadata.Keys)
                {
                    metadataXml.Append("<" + metadataName + ">");
                    metadataXml.Append(metadata[metadataName]);
                    metadataXml.Append("</" + metadataName + ">");
                }
                metadataXml.Append("</Metadata>");
                return metadataXml.ToString();
            }
            set { throw new NotSupportedException(); }
        }

        protected abstract void GetMetadata(Dictionary<string, object> metadata);

        public ISchemaDefinition MetadataXmlSchema
        {
            get
            {
                ISchemaDefinition schema = EclProvider.HostServices.CreateSchemaDefinition("Metadata", EclProvider.EcommerceEclNs.NamespaceName);
                schema.Fields.Add(EclProvider.HostServices.CreateSingleLineTextFieldDefinition("Id", "Id", 0, 1));
                schema.Fields.Add(EclProvider.HostServices.CreateSingleLineTextFieldDefinition("Name", "Name", 0, 1));
                this.BuildMetadataXmlSchema(schema);
                return schema;
            }
        }

        protected abstract void BuildMetadataXmlSchema(ISchemaDefinition schema);

        public virtual string ModifiedBy
        {
            get { return CreatedBy; }
        }

        public abstract IEclUri ParentId { get; }
        public abstract string DisplayTypeId { get; }

        public virtual string Dispatch(string command, string payloadVersion, string payload, out string responseVersion)
        {
            throw new NotSupportedException();
        }

        public virtual IContentLibraryItem Save(bool readback)
        {
            // as saving isn't supported, the result of saving is always the item itself
            return readback ? this : null;
        }

        public abstract string GetDirectLinkToPublished(IList<ITemplateAttribute> attributes);
    }
}
