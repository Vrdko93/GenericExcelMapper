$(document).ready(function () {

	loadTemplates();
	
	$("#designerTemplate").change(function () {

	    const templateId =
	        $(this).val();

	    const versionSelect =
	        $("#designerTemplateVersion");
			
		$("#newVersion").prop(
			"disabled",
			!templateId
		);

	    versionSelect.empty();

	    versionSelect.append(
	        $("<option>")
	            .val("")
	            .text("Select Version")
	    );

	    if (!templateId) {

	        versionSelect.prop(
	            "disabled",
	            true
	        );

	        return;
	    }

	    loadTemplateVersions(templateId);
	});
	
	$("#designerTemplateVersion").change(function () {

	    const templateVersionId =
	        $(this).val();

	    if (!templateVersionId) {

	        $("#versionStatus").hide();
	        return;
	    }

	    loadTargetFields(templateVersionId);

	    displayVersionStatus(
	        templateVersionId
	    );
	});
	
	$("#newTemplate").click(function () {

	    $("#newTemplateName").val("");
	    $("#newTemplateDescription").val("");

	    $("#newTemplateForm").show();
	});
	
	$("#cancelTemplate").click(function () {

	    $("#newTemplateForm").hide();

	    $("#newTemplateName").val("");
	    $("#newTemplateDescription").val("");
	});
	
	$("#newVersion").click(function () {

	    if (!$("#designerTemplate").val()) {
	        return;
	    }

	    $("#newVersionName").val("");
	    $("#newVersionEffectiveDate").val("");
	    $("#newVersionPublishedBy").val("");

	    $("#newVersionForm").show();
	});
	
	$("#cancelVersion").click(function () {

	    $("#newVersionForm").hide();

	    $("#newVersionName").val("");
	    $("#newVersionEffectiveDate").val("");
	    $("#newVersionPublishedBy").val("");
	});
	
	$("#saveTemplate").click(function () {

	    const name =
	        $("#newTemplateName").val().trim();

	    const description =
	        $("#newTemplateDescription").val().trim();

	    if (!name) {

	        showDesignerMessage(
	            "Template name is required.",
	            "error"
	        );

	        return;
	    }

	    const request = {
	        name: name,
	        description: description || null
	    };

	    $.ajax({

	        url: "/api/templates",

	        method: "POST",

	        contentType: "application/json",

	        data: JSON.stringify(request),

	        success: function (template) {

	            $("#newTemplateForm").hide();

	            showDesignerMessage(
	                "Template created successfully.",
	                "success"
	            );

	            loadTemplatesAndSelect(
	                template.id
	            );
	        },

	        error: function (xhr) {

	            console.error(xhr);

	            showDesignerMessage(
	                "Could not create template.",
	                "error"
	            );
	        }
	    });
	});
	
	$("#saveVersion").click(function () {

	    const templateId =
	        $("#designerTemplate").val();

	    const version =
	        $("#newVersionName").val().trim();

	    const effectiveDate =
	        $("#newVersionEffectiveDate").val();

	    const publishedBy =
	        $("#newVersionPublishedBy").val().trim();

	    if (!templateId) {

	        showDesignerMessage(
	            "Please select a template.",
	            "error"
	        );

	        return;
	    }

	    if (!version) {

	        showDesignerMessage(
	            "Version is required.",
	            "error"
	        );

	        return;
	    }

	    const request = {
	        version: version,
	        effectiveDate: effectiveDate || null,
	        publishedBy: publishedBy || null
	    };

	    $.ajax({

	        url:
	            "/api/templates/" +
	            templateId +
	            "/versions",

	        method: "POST",

	        contentType: "application/json",

	        data: JSON.stringify(request),

	        success: function (createdVersion) {

	            $("#newVersionForm").hide();

	            showDesignerMessage(
	                "Template version created successfully.",
	                "success"
	            );

	            loadTemplateVersionsAndSelect(
	                templateId,
	                createdVersion.id
	            );
	        },

	        error: function (xhr) {

	            console.error(xhr);

	            showDesignerMessage(
	                "Could not create template version.",
	                "error"
	            );
	        }
	    });
	});
	
	$("#activateVersion").click(function () {

	    const versionId =
	        $("#designerTemplateVersion").val();

	    const templateId =
	        $("#designerTemplate").val();

	    if (!versionId || !templateId) {
	        return;
	    }

	    const confirmed =
	        confirm(
	            "Activate this template version? " +
	            "The currently active version will become inactive."
	        );

	    if (!confirmed) {
	        return;
	    }

	    $.ajax({

	        url:
	            "/api/templates/versions/" +
	            versionId +
	            "/activate",

	        method: "PUT",

	        success: function () {

	            showDesignerMessage(
	                "Template version activated successfully.",
	                "success"
	            );

	            loadTemplateVersionsAndSelect(
	                templateId,
	                versionId
	            );
	        },

	        error: function (xhr) {

	            console.error(xhr);

	            showDesignerMessage(
	                "Could not activate template version.",
	                "error"
	            );
	        }
	    });
	});
	
	$("#addTargetField").click(function () {

	    editingTargetFieldId = null;

	    clearTargetFieldForm();

	    $("#targetFieldForm h3")
	        .text("Add Target Field");

	    $("#saveTargetField")
	        .text("Save Field");

	    $("#targetFieldForm").show();
	});

	$("#cancelTargetField").click(function () {

	    editingTargetFieldId = null;

	    $("#targetFieldForm").hide();

	    clearTargetFieldForm();

	    $("#targetFieldForm h3")
	        .text("Add Target Field");

	    $("#saveTargetField")
	        .text("Save Field");
	});
	
	$("#saveTargetField").click(function () {

	    const templateVersionId =
	        $("#designerTemplateVersion").val();

	    const name =
	        $("#fieldName").val().trim();

	    const key =
	        $("#fieldKey").val().trim();


			if (!name || !key) {
			    showDesignerMessage(
			        "Name and key are required.",
			        "error"
			    );
			    return;
			}

	    const request = {

	        templateVersionId:
	            parseInt(templateVersionId),

	        name: name,

	        key: key,

	        dataType:
	            $("#fieldDataType").val(),

	        mandatory:
	            $("#fieldMandatory").is(":checked"),

	        defaultValue:
	            $("#fieldDefaultValue").val().trim() || null,

	        description:
	            $("#fieldDescription").val().trim() || null
	    };

		const isEditing =
		    editingTargetFieldId !== null;

		const url =
		    isEditing
		        ? "/api/target-fields/" + editingTargetFieldId
		        : "/api/target-fields";

		const method =
		    isEditing
		        ? "PUT"
		        : "POST";
		
		$.ajax({

		    url: url,

		    method: method,

		    contentType: "application/json",

		    data: JSON.stringify(request),

		    success: function () {

		        const message =
		            isEditing
		                ? "Target field updated successfully."
		                : "Target field created successfully.";

		        editingTargetFieldId = null;

		        $("#targetFieldForm").hide();

		        clearTargetFieldForm();

		        loadTargetFields(
		            templateVersionId
		        );

		        showDesignerMessage(
		            message,
		            "success"
		        );
		    },

		    error: function (xhr) {

		        console.error(xhr);

		        showDesignerMessage(
		            isEditing
		                ? "Could not update target field."
		                : "Could not create target field.",
		            "error"
		        );
		    }
		});
	});
	
	$("#addValidationRule").click(function () {

	    editingValidationRuleId = null;

	    $("#validationRuleType").val("");
	    $("#validationRuleValue").val("");
	    $("#validationRuleErrorMessage").val("");
	    $("#validationRuleSequence").val("");
	    $("#validationRuleActive").prop("checked", true);

	    $("#validationRuleForm h3")
	        .text("Add Validation Rule");

	    $("#saveValidationRule")
	        .text("Save Rule");

	    $("#validationRuleForm").show();
	});
	
	$("#cancelValidationRule").click(function () {

	    editingValidationRuleId = null;

	    $("#validationRuleForm").hide();

	    $("#validationRuleForm h3")
	        .text("Add Validation Rule");

	    $("#saveValidationRule")
	        .text("Save Rule");
	});
	
	$("#saveValidationRule").click(function () {

	    if (!currentValidationFieldId) {
	        return;
	    }

	    const ruleType =
	        $("#validationRuleType").val();

	    const ruleValue =
	        $("#validationRuleValue").val().trim();

	    const errorMessage =
	        $("#validationRuleErrorMessage").val().trim();

	    const sequence =
	        $("#validationRuleSequence").val();

	    const active =
	        $("#validationRuleActive").is(":checked");

	    if (!ruleType) {

	        showDesignerMessage(
	            "Rule type is required.",
	            "error"
	        );

	        return;
	    }

	    if (!sequence) {

	        showDesignerMessage(
	            "Sequence is required.",
	            "error"
	        );

	        return;
	    }

	    const request = {
	        ruleType: ruleType,
	        ruleValue: ruleValue || null,
	        errorMessage: errorMessage || null,
	        sequence: parseInt(sequence),
	        active: active
	    };

		const isEditing =
		    editingValidationRuleId !== null;

		const url =
		    isEditing
		        ? "/api/templates/validation-rules/" +
		          editingValidationRuleId
		        : "/api/templates/fields/" +
		          currentValidationFieldId +
		          "/validation-rules";

		const method =
		    isEditing ? "PUT" : "POST";
		
			$.ajax({

			    url: url,
			    method: method,

			    contentType: "application/json",

			    data: JSON.stringify(request),

			    success: function () {

			        const message =
			            isEditing
			                ? "Validation rule updated successfully."
			                : "Validation rule created successfully.";

			        editingValidationRuleId = null;

			        $("#validationRuleForm").hide();

			        reloadValidationRules();

			        showDesignerMessage(
			            message,
			            "success"
			        );
			    },

			    error: function (xhr) {

			        console.error(xhr);

			        showDesignerMessage(
			            isEditing
			                ? "Could not update validation rule."
			                : "Could not create validation rule.",
			            "error"
			        );
			    }
			});
	});
});

let editingTargetFieldId = null;
let currentTemplateVersions = [];
let currentValidationFieldId = null;
let editingValidationRuleId = null;

function loadTemplates() {

    $.ajax({
        url: "/api/templates",
        method: "GET",

        success: function (templates) {

            const select =
                $("#designerTemplate");

            select.empty();

            select.append(
                $("<option>")
                    .val("")
                    .text("Select Template")
            );

            templates.forEach(function (template) {

                select.append(
                    $("<option>")
                        .val(template.id)
                        .text(template.name)
                );
            });
        },

        error: function (xhr) {

            console.error(xhr);

            showDesignerMessage(
                "Could not load templates.",
                "error"
            );
        }
    });
}

function loadTemplateVersions(templateId) {

    $.ajax({

        url:
            "/api/templates/" +
            templateId +
            "/versions",

        method: "GET",

        success: function (versions) {

			currentTemplateVersions = versions;
			
            const select =
                $("#designerTemplateVersion");

            select.empty();

            select.append(
                $("<option>")
                    .val("")
                    .text("Select Version")
            );

            versions.forEach(function (version) {

                let label =
                    version.version;

                if (version.active) {
                    label += " (Active)";
                }

                select.append(
                    $("<option>")
                        .val(version.id)
                        .text(label)
                );
            });

            select.prop(
                "disabled",
                false
            );
        },

        error: function (xhr) {

            console.error(xhr);

            showDesignerMessage(
                "Could not load template versions.",
                "error"
            );
        }
    });
}

function loadTemplatesAndSelect(templateId) {

    $.ajax({

        url: "/api/templates",

        method: "GET",

        success: function (templates) {

            const select =
                $("#designerTemplate");

            select.empty();

            select.append(
                $("<option>")
                    .val("")
                    .text("Select Template")
            );

            templates.forEach(function (template) {

                select.append(
                    $("<option>")
                        .val(template.id)
                        .text(template.name)
                );
            });

            select.val(templateId);
			$("#newVersion").prop("disabled", false);

            $("#designerTemplateVersion")
                .empty()
                .append(
                    $("<option>")
                        .val("")
                        .text("Select Version")
                )
                .prop("disabled", true);
        }
    });
}

function loadTemplateVersionsAndSelect(
    templateId,
    versionId
) {

    $.ajax({

        url:
            "/api/templates/" +
            templateId +
            "/versions",

        method: "GET",

        success: function (versions) {
			
			currentTemplateVersions = versions;

            const select =
                $("#designerTemplateVersion");

            select.empty();

            select.append(
                $("<option>")
                    .val("")
                    .text("Select Version")
            );

            versions.forEach(function (version) {

                let label = version.version;

                if (version.active) {
                    label += " (Active)";
                }

                select.append(
                    $("<option>")
                        .val(version.id)
                        .text(label)
                );
            });

            select
                .prop("disabled", false)
                .val(versionId);

            loadTargetFields(versionId);
			displayVersionStatus(versionId);
        }
    });
}

function displayVersionStatus(versionId) {

    const version =
        currentTemplateVersions.find(
            function (item) {
                return String(item.id) === String(versionId);
            }
        );

    if (!version) {
        $("#versionStatus").hide();
        return;
    }

    $("#versionStatus").show();

    if (version.active) {

        $("#versionStatusText")
            .text("Active");

        $("#activateVersion").hide();

    } else {

        $("#versionStatusText")
            .text("Draft / Inactive");

        $("#activateVersion").show();
    }
}

function loadTargetFields(templateVersionId) {

    $.ajax({

        url: "/api/target-fields",

        method: "GET",

        data: {
            templateVersionId:
                templateVersionId
        },

        success: function (fields) {

            displayTargetFields(fields);

            showDesignerMessage(
                "Target fields loaded successfully.",
                "success"
            );
        },

        error: function (xhr) {

            console.error(xhr);

            $("#targetFieldsTable tbody")
                .empty();

            showDesignerMessage(
                "Could not load target fields.",
                "error"
            );
        }
    });
}


function displayTargetFields(fields) {

    const tbody =
        $("#targetFieldsTable tbody");

    tbody.empty();

    if (!fields || fields.length === 0) {

        tbody.append(
            $("<tr>").append(
                $("<td>")
                    .attr("colspan", "8")
                    .text(
                        "No target fields found."
                    )
            )
        );

        return;
    }

    fields.forEach(function (field, index) {

        const row =
            $("<tr>");

        row.append(
            $("<td>").text(
                field.sequence
            )
        );

        row.append(
            $("<td>").text(
                field.name
            )
        );

        row.append(
            $("<td>").text(
                field.key
            )
        );

        row.append(
            $("<td>").text(
                field.dataType
            )
        );

        row.append(
            $("<td>").text(
                field.mandatory
                    ? "Yes"
                    : "No"
            )
        );

        row.append(
            $("<td>").text(
                field.defaultValue || ""
            )
        );

        row.append(
            $("<td>").text(
                field.description || ""
            )
        );
		
		const moveUpButton =
		    $("<button>")
		        .addClass("move-field-button")
		        .text("↑")
		        .attr("title", "Move Up")
		        .prop("disabled", index === 0)
		        .click(function () {
		            moveTargetField(field.id, "move-up");
		        });

		const moveDownButton =
		    $("<button>")
		        .addClass("move-field-button")
		        .text("↓")
		        .attr("title", "Move Down")
		        .prop("disabled", index === fields.length - 1)
		        .click(function () {
		            moveTargetField(field.id, "move-down");
		        });

		const editButton =
		    $("<button>")
		        .addClass("edit-field-button")
		        .text("Edit")
		        .click(function () {
		            editTargetField(field);
		        });

		const deleteButton =
		    $("<button>")
		        .addClass("delete-field-button")
		        .text("Delete")
		        .click(function () {
		            deleteTargetField(field);
		        });
		
		const rulesButton =
				    $("<button>")
				        .addClass("rules-field-button")
				        .text("Rules")
				        .click(function () {
				            loadValidationRules(field);
				        });

		const actionCell =
				    $("<td>")
				        .append(moveUpButton)
				        .append(moveDownButton)
				        .append(editButton)
				        .append(deleteButton)
						.append(rulesButton);

				row.append(actionCell);

        tbody.append(row);
    });
}

function moveTargetField(fieldId, direction) {

    $.ajax({

        url:
            "/api/target-fields/" +
            fieldId +
            "/" +
            direction,

        method: "PUT",

        success: function () {

            const templateVersionId =
                $("#designerTemplateVersion").val();

            loadTargetFields(
                templateVersionId
            );

            showDesignerMessage(
                "Target field order updated.",
                "success"
            );
        },

        error: function (xhr) {

            console.error(xhr);

            showDesignerMessage(
                "Could not update target field order.",
                "error"
            );
        }
    });
}

function editTargetField(field) {

    editingTargetFieldId = field.id;

    $("#fieldName")
        .val(field.name);

    $("#fieldKey")
        .val(field.key);

    $("#fieldDataType")
        .val(field.dataType);


    $("#fieldMandatory")
        .prop(
            "checked",
            field.mandatory
        );

    $("#fieldDefaultValue")
        .val(
            field.defaultValue || ""
        );

    $("#fieldDescription")
        .val(
            field.description || ""
        );

    $("#targetFieldForm h3")
        .text("Edit Target Field");

    $("#saveTargetField")
        .text("Update Field");

    $("#targetFieldForm").show();
}

function deleteTargetField(field) {

    const confirmed =
        confirm(
            'Are you sure you want to delete "' +
            field.name +
            '"?'
        );

    if (!confirmed) {
        return;
    }

    $.ajax({

        url:
            "/api/target-fields/" +
            field.id,

        method: "DELETE",

        success: function () {

            const templateVersionId =
                $("#designerTemplateVersion").val();

            loadTargetFields(
                templateVersionId
            );

            showDesignerMessage(
                "Target field deleted successfully.",
                "success"
            );
        },

        error: function (xhr) {

            console.error(xhr);

            showDesignerMessage(
                "Target field could not be deleted because it may already be in use.",
                "error"
            );
        }
    });
}

function showDesignerMessage(
    message,
    type
) {

    const container =
        $("#designerMessage");

    container
        .removeClass(
            "designer-success designer-error"
        )
        .text(message);

    if (type === "success") {
        container.addClass(
            "designer-success"
        );
    }

    if (type === "error") {
        container.addClass(
            "designer-error"
        );
    }
}

function clearTargetFieldForm() {

    $("#fieldName").val("");
    $("#fieldKey").val("");
    $("#fieldDataType").val("STRING");
    $("#fieldSequence").val("");
    $("#fieldMandatory").prop("checked", false);
    $("#fieldDefaultValue").val("");
    $("#fieldDescription").val("");
}

function loadValidationRules(field) {

    currentValidationFieldId = field.id;

    $("#validationRulesFieldName")
        .text(field.name);

    $("#validationRulesPanel").show();

    $.ajax({

        url:
            "/api/templates/fields/" +
            field.id +
            "/validation-rules",

        method: "GET",

        success: function (rules) {

            displayValidationRules(rules);
        },

        error: function (xhr) {

            console.error(xhr);

            showDesignerMessage(
                "Could not load validation rules.",
                "error"
            );
        }
    });
}

function displayValidationRules(rules) {

    const tbody =
        $("#validationRulesTable tbody");

    tbody.empty();

    if (rules.length === 0) {

        const row = $("<tr>");

        row.append(
            $("<td>")
                .attr("colspan", 6)
                .text("No validation rules defined.")
        );

        tbody.append(row);

        return;
    }

    rules.forEach(function (rule) {

        const row = $("<tr>");

        row.append(
            $("<td>").text(rule.sequence)
        );

        row.append(
            $("<td>").text(rule.ruleType)
        );

        row.append(
            $("<td>").text(rule.ruleValue || "")
        );

        row.append(
            $("<td>").text(rule.errorMessage || "")
        );

        row.append(
            $("<td>").text(
                rule.active ? "Yes" : "No"
            )
        );

		const editButton =
		    $("<button>")
		        .addClass("edit-rule-button")
		        .text("Edit")
		        .click(function () {
		            editValidationRule(rule);
		        });

		const deleteButton =
		    $("<button>")
		        .addClass("delete-rule-button")
		        .text("Delete")
		        .click(function () {
		            deleteValidationRule(rule);
		        });

		row.append(
		    $("<td>")
		        .append(editButton)
		        .append(deleteButton)
		);

        tbody.append(row);
    });
}

function reloadValidationRules() {

    if (!currentValidationFieldId) {
        return;
    }

    $.ajax({

        url:
            "/api/templates/fields/" +
            currentValidationFieldId +
            "/validation-rules",

        method: "GET",

        success: function (rules) {
            displayValidationRules(rules);
        },

        error: function (xhr) {
            console.error(xhr);
        }
    });
}

function editValidationRule(rule) {

    editingValidationRuleId = rule.id;

    $("#validationRuleType")
        .val(rule.ruleType);

    $("#validationRuleValue")
        .val(rule.ruleValue || "");

    $("#validationRuleErrorMessage")
        .val(rule.errorMessage || "");

    $("#validationRuleSequence")
        .val(rule.sequence);

    $("#validationRuleActive")
        .prop("checked", rule.active);

    $("#validationRuleForm h3")
        .text("Edit Validation Rule");

    $("#saveValidationRule")
        .text("Update Rule");

    $("#validationRuleForm").show();
}

function deleteValidationRule(rule) {

    const confirmed =
        confirm(
            'Are you sure you want to delete "' +
            rule.ruleType +
            '" validation rule?'
        );

    if (!confirmed) {
        return;
    }

    $.ajax({

        url:
            "/api/templates/validation-rules/" +
            rule.id,

        method: "DELETE",

        success: function () {

            reloadValidationRules();

            showDesignerMessage(
                "Validation rule deleted successfully.",
                "success"
            );
        },

        error: function (xhr) {

            console.error(xhr);

            showDesignerMessage(
                "Could not delete validation rule.",
                "error"
            );
        }
    });
}