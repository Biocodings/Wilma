﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Newtonsoft.Json;

namespace epam.wilma_service_api.StubClasses
{
    public class Template
    {
        [JsonProperty("name")]
        public string Name { get; set; }

        [JsonProperty("type")]
        public TemplateTypes Type { get; set; }

        [JsonProperty("resource")]
        public byte[] Resource { get; set; }
    }

    public enum TemplateTypes
    {
        XML,
        TEXT,
        HTML,
        JSON,
        XMLFILE,
        HTMLFILE,
        TEXTFILE,
        JSONFILE,
        EXTERNAL
    }
}
