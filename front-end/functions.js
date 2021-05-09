$(document).ready(function(){
    // initiate the line chart 
    initLineChart(3);

    $('[data-toggle="tooltip"]').tooltip({
        trigger : 'hover'
    });    
    var actions = '<a class="add" title="Add" data-toggle="tooltip"><i class="material-icons">&#xE03B;</i></a>' + 
        '<a class="edit" title="Edit" data-toggle="tooltip"><i class="material-icons">&#xE254;</i></a>' + 
        '<a class="delete" title="Delete" data-toggle="tooltip"><i class="material-icons">&#xE872;</i></a>';
    // Append table with add row form on add new button click
    $(".add-new").click(function(){
        $(this).attr("style", "display:none");
        var index = $("table#examples tbody#task-" + cur_task + " tr:last-child").index();
        var row = '<tr>' +
            '<td><input type="text" class="form-control" name="input string" id="input-example"></td>' +
            '<td class="icon"><select id="output-example" class="form-control"><option selected>Accept</option><option>Reject</option></select></td>' +
            '<td class="icon">' + actions + '</td>' +
        '</tr>';
        $("table#examples tbody#task-" + cur_task).append(row);     
        $("table#examples tbody#task-" + cur_task + " tr").eq(index + 1).find(".add, .edit").toggle();
        $('[data-toggle="tooltip"]').tooltip();
    });
    // Add row on add button click
    $(document).on("click", "table .add", function(){
        var empty = false;
        var input = $(this).parents("tr").find('input[type="text"]');
        input.each(function(){
            $(this).parent("td").html($(this).val());
        });

        var output = $(this).parents("tr").find('select');
        output.each(function(){
            if($(this).val() == "Accept") {
                $(this).parent("td").html('<i class="material-icons match">check</i>');
            } else {
                $(this).parent("td").html('<i class="material-icons unmatch">close</i>');
            }
        });

        $(this).parents("tr").find(".add, .edit").toggle();
        $(this).parents("tr").find(".move").removeAttr("style");
        $(".add-new").removeAttr("style");    
    });
    // Edit row on edit button click
    $(document).on("click", "table#examples .edit", function(){        
        $(this).parents("tr").find("td:not(:last-child)").each(function(){
            if($(this).hasClass('icon')) {
                var s = '<select id="output-example" class="form-control"><option selected>';
                if($(this).text() == 'check') {
                    s += 'Accept' + '</option><option>' + 'Reject';
                } else {
                    s += 'Reject' + '</option><option>' + 'Accept';
                }
                s+= '</option></select>';

                $(this).html(s);
            } else {
                $(this).html('<input type="text" class="form-control" value="' + $(this).text() + '">');
            }
        });     
        $(this).parents("tr").find(".add, .edit").toggle();
        $(".add-new").attr('style', 'display:none');
    });
    // Delete row on delete button click
    $(document).on("click", ".delete", function(){
        $(this).parents("tr").remove();
        $(".add-new").removeAttr("style");

        // remove any tooltips
        $('div.tooltip').remove();
    });

    $(document).on("click", "table#synthetic .move", function(){
        var input = $(this).parents("tr").find("td:first-child").text();
        var output = $(this).parents("tr").find("td:nth-child(2)").html();
        var row = '<tr>' +
            '<td>' + escapeHtml(input) + '</td>' +
            '<td class="icon">' + output + '</td>' +
            '<td class="icon">' + actions + '</td>' +
            '</tr>';
        $("table#examples tbody#task-" + cur_task).append(row);
        $('[data-toggle="tooltip"]').tooltip();
        $(this).parents("tr").remove();
    });

    $(document).on("click", "table#synthetic .edit", function(){        
        $(this).parents("tr").find("td:not(:last-child)").each(function(){
            if($(this).hasClass('icon')) {
                var s = '<select id="output-example" class="form-control"><option selected>';
                if($(this).text() == 'check') {
                    s += 'Accept' + '</option><option>' + 'Reject';
                } else {
                    s += 'Reject' + '</option><option>' + 'Accept';
                }
                s+= '</option></select>';

                $(this).html(s);
            } else {
                $(this).html('<input type="text" class="form-control" value="' + $(this).text() + '">');
            }
        });

        $(this).parents("tr").find(".move").attr("style", "display:none");
        $(this).parents("tr").find(".add, .edit").toggle();
    });

    // hide the context menu when clicking anywhere in the website
    $('body').on("click", function() {
        $("#context-menu").removeClass("show").hide();
    });

    // Action when clicking on a menu item
    $("#context-menu a").on("click", function() {
        // dismiss the context menu
        $(this).parent().removeClass("show").hide();

        var command = $(this).attr("id");
        if(command == 'prioritize') {
            $('#' + clickedNode.id + ' text').attr("style", "fill-opacity: 1; fill: #ffc107;");
            $('#' + clickedNode.id + ' circle').attr("style", "fill: #ffc107;");
            $('#' + clickedNode.id + ' circle').attr("stroke", "#ffc107");
            prioritized.push(clickedNode);

            // add to the breadcrumb trail of tree annotations
            var breadcrumb = $('#tree-annotations');
            var label = '<span class="badge badge-pill annotation must-have">' + escapeHtml(clickedNode.regex) + '</span>' 
                + '<button id="' + buttonId + '" class="removable" onclick="removeTreeAnnotation(this.id)">x</button>';
            breadcrumb.append(label);

            // remove the display:none property to show the breadcrumb trail
            breadcrumb.removeAttr('style');
            breadcrumb.attr('style', "margin-bottom: 15px;")

            buttonId++;
        } else if(command == 'avoid') {
            $('#' + clickedNode.id + ' text').attr("style", "fill-opacity: 1; fill: #6c757d;");
            $('#' + clickedNode.id + ' circle').attr("style", "fill: #6c757d;");
            $('#' + clickedNode.id + ' circle').attr("stroke", "#6c757d");
            avoided.push(clickedNode);

            // add to the breadcrumb trail of tree annotations
            var breadcrumb = $('#tree-annotations');
            var label = '<span class="badge badge-pill annotation not-have">' + escapeHtml(clickedNode.regex) + '</span>' 
                + '<button id="' + buttonId + '" class="removable" onclick="removeTreeAnnotation(this.id)">x</button>';
            breadcrumb.append(label);

            // remove the display:none property to show the breadcrumb trail
            breadcrumb.removeAttr('style');
            breadcrumb.attr('style', "margin-bottom: 15px;")

            buttonId++;
        } else if(command == 'undo') {
            $('#' + clickedNode.id + ' text').attr("style", "fill-opacity: 1;");
            var percent = 1 - clickedNode.number / sum;
            var bg_color = calculateColor(percent, "#2874A6", "#FFFFFF");
            $('#' + clickedNode.id + ' circle').attr("style", "fill: " + bg_color + ";");
            var stroke_color = clickedNode.children || clickedNode._children ? "steelblue" : "#00c13f"
            $('#' + clickedNode.id + ' circle').attr("stroke", stroke_color);
            if(prioritized.includes(clickedNode)) {
                const index = prioritized.indexOf(clickedNode);
                prioritized.splice(index, 1);
            }  

            if (avoided.includes(clickedNode)) {
                const index = avoided.indexOf(clickedNode);
                avoided.splice(index, 1);
            }

            // remove from the breadcrumb trail of tree annotations
            $('#tree-annotations span').each(function() {
                var text = $(this).text();
                if(text == clickedNode.regex) {
                    // this annotation and its close button should be removed
                    $(this).next().remove();
                    $(this).remove();
                }
            });

            if($('#tree-annotations span.must-have').length == 0 && $('#tree-annotations span.not-have').length == 0) {
                // the breadcrumb trail is empty, so hide it
                $('#tree-annotations').attr('style', "display:none; margin-bottom: 15px;")
            }
        }
    });
});

window.onbeforeunload = function(){
   socket.send("Window closed");
}

var cur_task = "0";

function changeTask() {
    var task_id = $('#change-task').val();
    cur_task = task_id;
    $('div#task-desc span').each(function() {
        var attr = $(this).attr('style');
        if (typeof attr !== typeof undefined && attr !== false) {
            if ($(this).attr('id') == "task-" + task_id) {
                $(this).removeAttr('style');
            }
        } else {
            $(this).attr('style', 'display:none');
        }
    });

    // clean synthesized examples and regexes of the previous task (if any)
    $('#regex-container div.regex').remove();

    // remove all annotations
    removeAllAnnotations();

    // reset the sample view
    resetSamples();

    // remove all auto-generated examples
    $('#synthetic tr').remove();
    // also remove the slider
    $('div#slider-container').empty();

    // hide the timeout label if any
    $("#timeout-label").attr("style", "display:none");
    $("#candidate-label").attr("style", "display:none");

    // reset the line charts
    if(chart != null) {
        chart.destroy();
        chart = null;
        // initiate the line chart 
        initLineChart(3);
    }

    if(chartLine != null) {
        chartLine.destroy();
        chartLine = null;
    }

    // remove any tree vis
    $("#tree").empty();
    $("#tree").removeAttr('style');
    // hide the tree vis label
    $("div#tree-container label").attr("style", "font-size:large; display:none;");

    // reset the tree annotation arrays
    prioritized = [];
    avoided = [];
    
    // show some initial examples
    $('table#examples tbody').each(function() {
        var attr = $(this).attr('style');
        if (typeof attr !== typeof undefined && attr !== false) {
            if ($(this).attr('id') == "task-" + task_id) {
                $(this).removeAttr('style');
            }
        } else {
            $(this).attr('style', 'display:none');
        }
    });

    // reset all global variables
    generalizations = {};
    synthetic_examples = {};
    buttonId = 1;
    generalized_chars = null;
    sel_regex = null;

    // send a signal to the server
    socket.send("Reset");
}

function removeAllAnnotations() {
    // remove breadcrumbs
    $('div#regex-annotations span.must-have').remove();
    $('div#regex-annotations span.probably-have').remove();
    $('div#regex-annotations span.not-have').remove();
    $('div#regex-annotations button').remove();
    $('div#tree-annotations span.must-have').remove();
    $('div#tree-annotations span.not-have').remove();
    $('div#tree-annotations button').remove();

    // hide the breadcrumb trails
    $('div#regex-annotations').attr('style', 'display:none; margin-bottom: 15px;');
    $('div#tree-annotations').attr('style', 'display:none; margin-bottom: 15px;');
}

var buttonId = 1;

var generalized_chars;

function highlightSelection(clr) {
    var selection;

    //Get the selected stuff
    if (window.getSelection)
        selection = window.getSelection();
    else if (typeof document.selection != "undefined")
        selection = document.selection;

    //Get a the selected content, in a range object
    var range = selection.getRangeAt(0);
    var selection_str = selection.toString();
    var is_safari = /^((?!chrome|android).)*safari/i.test(navigator.userAgent);

    //If the range spans some text, and inside a tag, set its css class.
    if (range && !selection.isCollapsed) {
        if (selection.anchorNode.parentNode == selection.focusNode.parentNode || is_safari) {
            var span = document.createElement('span');
            if(clr == 'must-include' || clr == 'exact-match') {
                span.className = 'must-have';
                generalized_chars = null;
            } else if (clr == 'probably-include') {
                span.className = 'probably-have';
                generalized_chars = null;
            } else if (clr == 'exclude') {
                span.className = 'not-have';
                generalized_chars = null;
            } else if (clr == 'char-family') {
                span.className = 'char-family';
                generalized_chars = escapeHtml(selection_str);
            }
            
            range.surroundContents(span);

            // if the user annotates a regex, add the annotated subexpression in the breadcrumb
            if(clr == 'must-include' || clr == 'probably-include' || clr == 'exclude') {
                var breadcrumb = $('#regex-annotations');
                var label = '<span class="badge badge-pill annotation ' + span.className + '">' + escapeHtml(selection_str) + '</span>' 
                    + '<button id="' + buttonId + '" class="removable" onclick="removeAnnotation(this.id)">x</button>';
                breadcrumb.append(label);
                
                // remove the display:none css property to show the annotation
                breadcrumb.removeAttr('style');
                breadcrumb.attr('style', 'margin-bottom: 15px;');

                buttonId++;
            }
        }
    }
}

function markRegexInTreeNode(clr) {
    var selection;

    //Get the selected stuff
    if (window.getSelection)
        selection = window.getSelection();
    else if (typeof document.selection != "undefined")
        selection = document.selection;

    //Get a the selected content, in a range object
    var range = selection.getRangeAt(0);

    //If the range spans some text, and inside a tag, set its css class.
    if (range && !selection.isCollapsed) {
        if (selection.anchorNode.parentNode == selection.focusNode.parentNode) {
            var className = "";
            if(clr == 'must-include') {
                className = 'must-have';
            } else if (clr == 'probably-include') {
                className = 'probably-have';
            } else if (clr == 'exclude') {
                className = 'not-have';
            } 

            // if the user annotates a regex, add the annotated subexpression in the breadcrumb
            if(clr == 'must-include' || clr == 'probably-include' || clr == 'exclude') {
                var breadcrumb = $('#regex-annotations');
                var label = '<span class="badge badge-pill annotation ' + className + '">' + escapeHtml(selection.toString()) + '</span>' 
                    + '<button id="' + buttonId + '" class="removable" onclick="removeAnnotation(this.id)">x</button>';
                breadcrumb.append(label);
                
                // remove the display:none css property to show the annotation
                breadcrumb.removeAttr('style');
                breadcrumb.attr('style', 'margin-bottom: 15px;');

                buttonId++;
            }
        }
    }
}

function addNewExample() {
    var input = document.getElementById('input-example');
    var output = document.getElementById('output-example');

    document.createElement('tr');
}

var socket; 

// Handle any errors that occur.
const socketErrorListener = (event) => {
    // console.log('WebSocket Error: ' + event);
};

// Make sure we're connected to the WebSocket before trying to send anything to the server
const socketOpenListener = (event) => {
    // send the code example to the backend for parsing and analysis
    console.log('Connected to the server.');
}

var logs = [];
var regex_array_copy = null;
// Handle messages sent by the backend.
const socketMessageListener = (event) => {
    // console.log(event.data);
    if (event.data.startsWith("regexes:")) {
        var regexJson = event.data.substring(event.data.indexOf(":") + 2);
        var regexArray = JSON.parse(regexJson);
        console.log("received " + regexArray.length + "regexes from the backend.");
        if(isPrompt) {
            // prompt a message
            if(regexArray.length == 5) {
                $('#promptMessage').text("The synthesizer has found 5 regexes that satisfy your examples. Do you want to pause and check the result?");
            } else {
                $('#promptMessage').text("The synthesizer has run for a while. Do you want to pause it and check the result?");
            }
            
            $('#promptModal').modal({
                backdrop: 'static',
                keyboard: false
            });
            // make a copy of the regex array so we can render it later if a user decides to pause the synthesizer and inspect after seeing the prompt message
            regex_array_copy = regexArray;
        } else {
            // the user has proactively pause or stop the synthesizer
            // just isplay the new synthesized regexes
            displayRegex(regexArray);
        }

        // reset isPrompt for the next iteration
        isPrompt = true;
    } else if (event.data.startsWith("examples:")) {
        console.log("received auto-generated examples from the backend.");

        // Store the generated examples
        var message = event.data.substring(event.data.indexOf(":") + 2);
        var selected_regexes = getSelectedRegexes();
        synthetic_examples[selected_regexes] = message;

        // display similar examples by default
        if(isDisplayWildExamples) {
            displaySyntheticExamples(message.split("\n")[1], true);
        } else {
            displaySyntheticExamples(message.split("\n")[0], false);
        }
    } else if (event.data.startsWith("heartbeat:")) {
        console.log("received synthesizer's heartbeat.");

        // sync message to update the line chart
        var message = event.data.substring(event.data.indexOf(":") + 2);
        var arr = JSON.parse(message);
        logs.push.apply(logs, arr);
        updateData(arr);
    } else if (event.data.startsWith("summary:")) {
        console.log("received summary data from the backend.");

        var message = event.data.substring(event.data.indexOf(":") + 2);
        if(message == 'none') {
            // no concrete programs have been reached so far
            $('div#programs').html('<label style="color:#dc3545;">The synthesizer has been busy with enumerating regexes with holes. It has not reached to a concrete regex yet. Do you want to check the enumeration process in the Search Tree tab?</label>');
        } else {
            var sample_data = JSON.parse(message);
            sample_data_cache[sample_data['sampler_type']] = sample_data;

            if(sample_data['sampler_type'] === 'MaxExamples'){
                showSummaryInTable(sample_data);
            }
        }
    } else if (event.data.startsWith("Tree:")) {
        console.log("received tree view data from the backend.");

        var message = event.data.substring(event.data.indexOf(":") + 2);
        var treeData = JSON.parse(message);
        BuildHorizontalTree(treeData);
    }
}

// Show a disconnected message when the WebSocket is closed.
const socketCloseListener = (event) => {
    if(socket) {
        console.error('Disconnected.');
    }
    // socket = new WebSocket('ws://3.236.11.113:8050/');
    socket = new WebSocket('ws://127.0.0.1:8070/');
    socket.addEventListener('open', socketOpenListener);
    socket.addEventListener('message', socketMessageListener);
    socket.addEventListener('close', socketCloseListener);
    socket.addEventListener('error', socketErrorListener);
};

// establish the connection to server
socketCloseListener();

// a global variable for keeping track of the number of examples used in the current synthesis iteration
// need to keep a separate variable because users may add new examples while waiting for synthesis to finish
var example_count = 0;
var continuous = 0;
var hasPausedOrStopped = false;
function synthesize(redraw) {
    // reset hasPausedOrStopped
    hasPausedOrStopped = false;

    // Gather input examples and their annotations
    var rows = $("#examples tbody#task-" + cur_task +" tr");

    if(redraw) {
        // redraw the line chart
        // address the y-axis labels based on the number of examples
        continuous = 0;
        example_count = rows.length;
        initLineChart(example_count);
    } else {
        // the user wants to continue the synthesis for another 20 seconds
        // keep the line chart from the previous iteration
        continuous ++;
    }

    // Reset background colors
    resetTableColors();

    // Reset the active tab to Correct Regexes
    $('#myTab a[href="#correctView"]').tab('show');

    var examples = '[';
    rows.each(function() {
        var input = $(this).children('td').eq(0);

        var output = $(this).children('td').eq(1);

        if(input.find('input').length || output.find('select').length){
            return;
        }

        examples += '{ "input" : "' + escapeJson(input.text()) + '", ' + '"exact" : [';
        if(input.children('.must-have').length > 0 && output.text() == 'check') {
            // marked as a literal on a positive example
            input.children('.must-have').each(function() {
                examples += '"' + escapeJson($(this).text()) + '", ';
            });
            examples = examples.substring(0, examples.length - 2);
        } 
        examples += '], "unmatch" : [';

        if(input.children('.must-have').length > 0 && output.text() == 'close') {
            input.children('.must-have').each(function() {
                examples += '"' + escapeJson($(this).text()) + '", ';
            });
            examples = examples.substring(0, examples.length - 2);
        }
        examples += '], "generalize" : [';

        if(input.children('.char-family').length > 0) {
            input.children('.char-family').each(function() {
                var text = $(this).text();
                examples += '"' + escapeJson(text) + '@@@' + generalizations[text] + '", ';
            });
            examples = examples.substring(0, examples.length - 2);
        }
        examples += '], ';

        examples += '"output" : ';
        if(output.text() == "check") {
            examples += 'true }';
        } else if (output.text() == "close") {
            examples += 'false }';
        }
        examples += ", "
    });
    
    if(examples.length > 0) {
        examples = examples.substring(0, examples.length - 2);
    }
    examples += "]";

    // Gather snthesized programs and their annotations if any
    var regexes = '[';
    var includes = '[';
    var breadcrumbs = $("div#regex-annotations span.must-have");
    breadcrumbs.each(function() {
        var include = unescapeHtml($(this).text());
        includes += '"' + include + '", ';
    });
    if(breadcrumbs.length > 0) {
        includes = includes.substring(0, includes.length - 2);
    }
    includes += ']';

    var excludes = '[';
    breadcrumbs = $("div#regex-annotations span.not-have");
    breadcrumbs.each(function() {
        var exclude = unescapeHtml($(this).text());
        excludes += '"' + exclude + '", ';
    });
    if(breadcrumbs.length > 0) {
        excludes = excludes.substring(0, excludes.length - 2);
    }
    excludes += ']';

    var maybes = '[';
    breadcrumbs = $("div#regex-annotations span.probably-have");
    breadcrumbs.each(function() {
        var maybe = unescapeHtml($(this).text());
        maybes += '"' + maybe + '", ';
    });
    if(breadcrumbs.length > 0) {
        maybes = maybes.substring(0, maybes.length - 2);
    }
    maybes += ']';    

    if(includes != '[]' || excludes != '[]' || maybes != '[]') {
        regexes += '{ "regex" : "", ' + '"include" : ' + includes + ', "exclude" : ' + excludes + ', "maybe" : ' + maybes + '}, ';
    }

    var regex_labels = $(".regex label");
    regex_labels.each(function() {
        var regex = $(this);
        regexes += '{ "regex" : "' + regex.text().trim() + '", ' + '"include" : [';
        if(regex.children('.must-have').length > 0) {
            regex.children('.must-have').each(function() {
                regexes += '"' + $(this).text() + '", ';
            });
            regexes = regexes.substring(0, regexes.length - 2);
        }
        regexes += '], "exclude" : [';

        if(regex.children('.not-have').length > 0) {
            regex.children('.not-have').each(function() {
                regexes += '"' + $(this).text() + '", ';
            });
            regexes = regexes.substring(0, regexes.length - 2);
        }
        regexes += '], "maybe" : [';

        if(regex.children('.probably-have').length > 0) {
            regex.children('.probably-have').each(function() {
                regexes += '"' + $(this).text() + '", ';
            });
            regexes = regexes.substring(0, regexes.length - 2);
        }
        regexes += ']}, ';        
    });

    if(regex_labels.length > 0 || includes != '[]' || excludes != '[]') {
        regexes = regexes.substring(0, regexes.length - 2);
    }
    regexes += "]";

    // Gather tree annotations if any
    var prioritized_branches = "";
    var p_breadcrumbs = $('#tree-annotations span.must-have');
    if(p_breadcrumbs.length > 0) {
        p_breadcrumbs.each(function() {
            prioritized_branches += $(this).text() + "&&";
        });
        prioritized_branches = prioritized_branches.substring(0, prioritized_branches.length - 2);
    } else {
        prioritized_branches = ",";
    }

    var avoided_branches = "";
    var a_breadcrumbs = $('#tree-annotations span.not-have');
    if(a_breadcrumbs.length > 0) {
        a_breadcrumbs.each(function() {
            avoided_branches += $(this).text() + "&&";
        });
        avoided_branches = avoided_branches.substring(0, avoided_branches.length - 2);
    } else {
        avoided_branches = ",";
    }

    socket.send("Synthesize Regexes: " + examples + '\n' + regexes + '\n' 
        + prioritized_branches + '\n' + avoided_branches);

    // clean the previous synthesized regexes and generated examples (if any)
    $("#regex-container div.regex").remove();
    $("#synthetic tr").remove();
    // reset the timeout label if any
    $("#timeout-label").attr("style", "display:none");
    $("#candidate-label").attr("style", "display:none");
    // also remove the slider
    $('div#slider-container').empty();
    // remove any tree vis
    $("#tree").empty();
    $("#tree").removeAttr('style');
    // hide the tree vis label
    $("div#tree-container label").attr("style", "font-size:large; display:none;");
    // remove sampled programs
    resetSamples();

    // clear the tree annotations
    prioritized = [];
    avoided = [];
}

var isPrompt = true;

function pause() {
    isPrompt = false;
    
    if(!hasPausedOrStopped) {
        socket.send("Summary: " + continuous);
        hasPausedOrStopped = true;
    }

    // let socket2 = new WebSocket('ws://3.236.11.113:8060/');
    let socket2 = new WebSocket('ws://127.0.0.1:8080/');
    socket2.onopen = function(e) {
        socket2.send("Pause");
        console.log("Pause sent.");
    };
}

function stop() {
    isPrompt = false;

    if(!hasPausedOrStopped) {
        socket.send("Summary: " + continuous);
        hasPausedOrStopped = true;
    }

    // let socket2 = new WebSocket('ws://3.236.11.113:8060/');
    let socket2 = new WebSocket('ws://127.0.0.1:8080/'); 
    socket2.onopen = function(e) {
        socket2.send("Stop");
        console.log("Stop sent.");
    };
}

function escapeHtml(unsafe) {
    return unsafe
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
}

function unescapeHtml(unsafe) {
    return unsafe
         .replace(/&amp;/g, "&")
         .replace(/&lt;/g, "<")
         .replace(/&gt;/g, ">")
         .replace(/&quot;/g, "\"")
         .replace(/&#039;/g, "'");
}

function escapeJson(unsafe) {
    return unsafe
        .replace(/\\/g, '\\\\')
        .replace(/"/g, '\\"');
}

function displayRegex(regexArray) {
    if(regexArray.length == 0) {
        // no satisfying programs are found
        $("#timeout-label").removeAttr('style');
        $("#timeout-label label").text("The synthesizer hasn't found a regex candidate that satisfy all examples so far. Do you want to click the Samples and Search Tree tabs to check if it is on the wrong direction?");
    } else {
        $("#candidate-label").removeAttr('style');
    }

    var count = 0;
    regexArray.forEach(function(regex) {
        $("#regex-container").append('<div class="form-check regex">' + 
            '<input class="form-check-input" type="checkbox" value="" id="regex' + count + '">' + 
            '<label class="form-check-label text-break" for="regex' + count + '">' + 
            escapeHtml(regex) +
            '</label></div>');
        count++;         
    });
}

function removeAnnotation(id) {
    var button = $("button#"+id);
    var annotation = button.prev().text();
    var type;
    if(button.prev().hasClass('must-have')) {
        type = "must-have";
    } else if(button.prev().hasClass('probably-have')) {
        type = "probably-have";
    } else {
        type = "not-have";
    }

    // remove the corresponding annotation in synthesized regexes
    var regexes = $(".regex label");
    regexes.each(function() {
        var regex = $(this);
        if(regex.text().includes(annotation)) {
            regex.children('.' + type).each(function() {
                if($(this).text() == annotation) {
                    $(this).replaceWith(escapeHtml($(this).text()));
                }
            });
        }
    });

    // remove the corresponding annotation in sampled regexes (if any)
    var samples = $(".sample");
    samples.each(function() {
        var regex = $(this);
        if(regex.text().includes(annotation)) {
            regex.children('.' + type).each(function() {
                if($(this).text() == annotation) {
                    $(this).replaceWith(escapeHtml($(this).text()));
                }
            });
        }
    });

    button.prev().remove();
    button.remove();

    if($('#regex-annotations span.must-have').length == 0 
        && $('#regex-annotations span.not-have').length == 0
        && $('#regex-annotations span.probably-have').length == 0) {
        // the breadcrumb trail is empty, so hide it
        $('#regex-annotations').attr('style', "display:none; margin-bottom: 15px;")
    }
}

function removeTreeAnnotation(id) {
    var button = $("button#"+id);
    var annotation = button.prev().text();
    var treeNode = null;
    if(button.prev().hasClass('must-have')) {
        for(let i = 0; i < prioritized.length; i++) {
            if(prioritized[i].regex == annotation) {
                treeNode = prioritized[i];

                // remove it from the array
                prioritized.splice(i, 1);
                break;
            }
        }
    } else {
        for(let i = 0; i < avoided.length; i++) {
            if(avoided[i].regex == annotation) {
                treeNode = avoided[i];

                 // remove it from the array
                avoided.splice(i, 1);
                break;
            }
        }
    }

    // if the tree annotation is added in the previous iteration
    // the prioritized and avoided arrays have been cleaned already
    // the treeNode will be null
    // so need to double check
    if(treeNode != null) {
        // reset tree node colors if the tree node has not been collapsed
        $('#' + treeNode.id + ' text').attr("style", "fill-opacity: 1;");
        var percent = 1 - treeNode.number / sum;
        var bg_color = calculateColor(percent, "#2874A6", "#FFFFFF");
        $('#' + treeNode.id + ' circle').attr("style", "fill: " + bg_color + ";");
        var stroke_color = treeNode.children || treeNode._children ? "steelblue" : "#00c13f"
        $('#' + treeNode.id + ' circle').attr("stroke", stroke_color);
    }
    

    button.prev().remove();
    button.remove();

    if($('#tree-annotations span.must-have').length == 0 && $('#tree-annotations span.not-have').length == 0) {
        // the breadcrumb trail is empty, so hide it
        $('#tree-annotations').attr('style', 'display:none; margin-bottom: 15px;');
    }
}

var sel_regex;
var synthetic_examples = {};
var isDisplayWildExamples = false;

function displaySimilarExamples() {
    isDisplayWildExamples = false;
    // check if we have generated the current selected regexes before
    var selected_regexes = getSelectedRegexes();
    if(selected_regexes != "") {
        var message = synthetic_examples[selected_regexes];
        if(message == null) {
            // generate examples for the selected regexes
            generateSyntheticExamples(selected_regexes);
        } else {
            // display the previously generated similar examples
            var similarExamples = message.split("\n")[0];
            displaySyntheticExamples(similarExamples, false);
        }
    }
}

function displayWildExamples() {
    isDisplayWildExamples = true;
     // check if we have generated the current selected regexes before
    var selected_regexes = getSelectedRegexes();
    if(selected_regexes != "") {
        var message = synthetic_examples[selected_regexes];
        if(message == null) {
            // generate examples for the selected regexes
            generateSyntheticExamples(selected_regexes);
        } else {
            // display the previously generated similar examples
            var wildExamples = message.split("\n")[1];
            displaySyntheticExamples(wildExamples, true);
        }
    }
}

function getSelectedRegexes() {
    // get selected regexes if any
    var regex_checkboxs = $(".regex input");
    var all_regexes = '[';
    var selected_regexes = '[';
    regex_checkboxs.each(function() {
        var checkbox = $(this);
        if(checkbox.is(':checked')) {
            selected_regexes += '"' + checkbox.next().text().trim() + '",'; 
        }
        all_regexes += '"' + checkbox.next().text().trim() + '",';
    });

    if(all_regexes.endsWith(",")) {
        all_regexes = all_regexes.substring(0, all_regexes.length - 1) + "]";   
    } else {
        alert("No regular expressions have been synthesized yet.");
        return "";
    }

    if(selected_regexes.endsWith(",")) {
        selected_regexes = selected_regexes.substring(0, selected_regexes.length - 1) + "]";
        sel_regex = JSON.parse(selected_regexes);
        return selected_regexes; 
    } else {
        // no regexes are selected
        alert("Please select one or more regular expressions first.");
        return "";
    }
}

function generateSyntheticExamples(regexes) {
    var rows = $("#examples tbody#task-" + cur_task + " tr");
    var examples = '[';
    rows.each(function() {
        var input = $(this).children('td').eq(0);
        var output = $(this).children('td').eq(1);
        if(output.text() == 'check') {
            examples += '"' + input.text().trim() + '",';
        } 
    });

    if(examples.endsWith(',')) {
        examples = examples.substring(0, examples.length - 1);
    } else {
        // no input examples
        alert("Please enter some examples first.");
        return;
    }

    examples += ']';

    socket.send("Generate Examples: " + examples + "\n" + regexes);
}

function displaySyntheticExamples(exampleJson, isWild) {
    // remove previous synthetic examples
    $("#synthetic tr").remove();
    $('div#slider-container').empty();

    var clusters = []; 
    var headers = [];
    // get the maximum of examples in a cluster
    var maxNum = 0;
    var cluster_counter = 0;
    
    // synthetic examples are now represented as a map in which key is the explanation of a cluster and value is a cluster of input-output examples
    var map = JSON.parse(exampleJson);
    for(var explanation in map) {
        headers[cluster_counter] = explanation;
        clusters[cluster_counter] = map[explanation];
        cluster_counter ++; 
    }

    clusters.forEach(function(cluster) {
        var count = 0;
        for(let example in cluster) {
            count++;
        }
        if(count > maxNum) {
            maxNum = count;
        }
    });

    if(maxNum == 0) {
        // no examples are generated
        if(isWild) {
            if(sel_regex.length == 1) {
                $('div#slider-container').append("<p>Oops, no examples are generated. Do you want to try clicking on 'Show me familiar examples' instead?</p>");
            } else {
                $('div#slider-container').append("<p>These selected regexes are logically the same. We cannot find examples that distinguish their behavior.</p>");
            }
        } else {
            if(sel_regex.length == 1) {
                $('div#slider-container').append("<p>Oops, no examples are generated. Do you want to try clicking on 'Show me corner cases' instead?</p>");
            } else {
                $('div#slider-container').append("<p>We cannot generate examples that distinguish these selected regexes based on the examples you give. "
                    + "Maybe these regexes are equivalent. Do you want to try clicking on 'Show me corner cases' to double check?</p>");
            }
        }
        return;
    }

    var num_to_display = maxNum > 5 ? 5 : maxNum;
    // generate the slider
    $('div#slider-container').append('<label for="exampleSlider">Number of Examples Per Cluster: </label><label id="sliderValue" style="margin-left:10px">' + num_to_display + '</label><br>');
    $('div#slider-container').append('<label style="margin:5px 10px 5px 10px;">0</label>' + 
            '<input type="range" class="custom-range" style="width: 80%; padding-top: 10px; margin:0px 10px 5px 10px;" id="exampleSlider" min="0" max="' + maxNum + '" step="1" value="' + num_to_display + '">' + 
            '<label id="maxSliderValue" style="margin:5px 10px 5px 10px;">' + maxNum + '</label>');

    // add a listener to the slider
    $('#exampleSlider').on('input', function(){
        // console.log(this.value);
        $('#sliderValue').text(this.value);

        var rows = $("#synthetic tr");

        var num_to_display = this.value;
        if(sel_regex.length > 1) {
            num_to_display ++;
        }

        var count = 0;
        rows.each(function(){
            var row = $(this);

            if(row.has('th').length == 1) {
                // new cluster
                count = 0;
            } else {
                var attr = row.attr('style');
                if(count < num_to_display) {
                    if (!(typeof attr == typeof undefined || attr == false)) {
                        row.removeAttr('style');
                    }
                } else {
                    if (typeof attr == typeof undefined || attr == false) {
                        row.attr('style', 'display:none');
                    }
                }

                count++;
            }
        });
    });

    if(sel_regex.length == 1) {
        // only need to show examples of one regex
        var i = 1;
        clusters.forEach(function(cluster) {
            // sort examples in the same cluster by length
            var arr = [];
            for(let example in cluster) {
                arr.push(example);
            }

            arr.sort(function(a, b){
                return a.length - b.length;
            });

            // try our best to show positive and negative examples next to each other after sorting
            var positives = [];
            var negatives = [];
            for(var j = 0; j < arr.length; j++) {
                var example = arr[j];
                if(cluster[example]) {
                    positives.push(example);
                } else {
                    negatives.push(example);
                }
            }
            // sort examples in array again to pair positive and negative examples
            var newArr = [];
            var len = positives.length > negatives.length ? negatives.length : positives.length; 
            var k = 0;
            for(; k < len; k++) {
                newArr.push(negatives[k]);
                newArr.push(positives[k]);
            }
            len = positives.length > negatives.length ? positives.length : negatives.length; 
            for(; k < len; k++) {
                // add the rest of examples
                if(len == positives.length) {
                    newArr.push(positives[k]);
                } else {
                    newArr.push(negatives[k]);
                }
            }

            arr = newArr;

            $("#synthetic tbody").append('<tr><th colspan="3" class="table-active" style="text-align: center;">Cluster ' + i + ': ' + headers[i-1] + '</th></tr>');
            
            var count = 0;
            for(var j = 0; j < arr.length; j++) {
                var example = arr[j];
                var display = ' style="display:none"';
                if(count < num_to_display) {
                    display = '';
                }

                var row;
                // if(isWild) {
                    // row = '<tr' + display + '><td>' + example + '</td>';
                    if(cluster[example]) {
                        row = '<tr' + display + '><td><span class="cluster2">' + example + '</span></td>';
                    } else {
                        // get the index of the failure-inducing character
                        if(example == ",0") {
                            // empty string
                            row = '<tr' + display + '><td><span class="cluster6"></span></td>'; 
                        } else {
                            var index = example.lastIndexOf(",");
                            var charIndex = parseInt(example.substring(index + 1));
                            var s1 = example.substring(0, charIndex);
                            var s2 = example.substring(charIndex, charIndex+1); 
                            var s3 = example.substring(charIndex+1, index);
                            row = '<tr' + display + '><td><span class="cluster2">' + escapeHtml(s1) + '</span><span class="cluster6">' + escapeHtml(s2) + '</span>' + escapeHtml(s3) + '</td>';
                        }
                    }
                // } else {
                //     var s1 = example.substring(0, i-1);
                //     var s2 = example.substring(i-1, i); 
                //     var s3 = example.substring(i);

                //     // row = '<tr' + display + '><td>' + s1 + '<span class="cluster' + classId + '">' + s2 + '</span>' + s3 + '</td>';
                //     if(cluster[example]) {
                //         // only show which character has been changed and use color to indicates the result
                //         // row = '<tr' + display + '><td>' + s1 + '<span class="cluster2">' + s2 + '</span>' + s3 + '</td>';
                //         // highlight each character based on the matching result
                //         row = '<tr' + display + '><td><span class="cluster2">' + example + '</span></td>';
                //     } else {
                //         row = '<tr' + display + '><td><span class="cluster2">' + s1 + '</span><span class="cluster6">' + s2 + '</span>' + s3 + '</td>';
                //     }
                // }

                var resultText;
                var resultClass;
                if(cluster[example] == true) {
                    resultText = 'check';
                    resultClass = 'match';
                } else {
                    resultText = 'close';
                    resultClass = 'unmatch';
                }

                $("#synthetic tbody").append(row +
                                      '<td class="icon"><i class="material-icons ' + resultClass + '">' + resultText + '</i></td>' + 
                                      '<td class="icon">' + 
                                            '<a class="add" title="Add" data-toggle="tooltip"><i class="material-icons">&#xE03B;</i></a>' + 
                                            '<a class="edit" title="Edit" data-toggle="tooltip"><i class="material-icons">&#xE254;</i></a>' + 
                                            '<a class="move" title="Add as a new example" data-toggle="tooltip"><i class="material-icons">add</i></a>' +
                                      '</td></tr>');
                count++;
            }
            i++;         
        });
    } else {
        // show distinguishing examples
        var i = 1;
        clusters.forEach(function(cluster) {
            $("#synthetic tbody").append('<tr><th colspan="' + (2 + sel_regex.length) + 
                    '" class="table-active" style="text-align: center;">Cluster ' + i + ': ' + headers[i-1] + '</th></tr>');
            var header = '<tr><td>Example</td>';
            for(j=0; j < sel_regex.length; j++) {
                header += '<td style="word-wrap: break-word">' + escapeHtml(sel_regex[j]) + '</td>';
            }
            header += '<td></td></tr>';

            $("#synthetic tbody").append(header);

            var count = 0; 
            for(let example in cluster) {
                var display = ' style="display:none"';
                if(count < num_to_display) {
                    display = '';
                }

                var row;
                // if(isWild) {
                //     row = '<tr' + display + '><td>' + example + '</td>';
                // } else {
                //     var s1 = example.substring(0, i-1);
                //     var s2 = example.substring(i-1, i); 
                //     var s3 = example.substring(i);

                //     row = '<tr' + display + '><td>' + s1 + '<span class="cluster' + classId + '">' + s2 + '</span>' + s3 + '</td>';
                // }

                row = '<tr' + display + '><td>' + escapeHtml(example) + '</td>';

                var results = cluster[example];
                for(j=0; j < results.length; j++) {
                    var resultText;
                    var resultClass;
                    if(results[j]== true) {
                        resultText = 'check';
                        resultClass = 'match';
                    } else {
                        resultText = 'close';
                        resultClass = 'unmatch';
                    }

                    row += '<td class="icon"><i class="material-icons ' + resultClass + '">' + resultText + '</i></td>';
                }

                row += '<td class="icon">' + 
                            '<a class="add" title="Add" data-toggle="tooltip"><i class="material-icons">&#xE03B;</i></a>' + 
                            '<a class="edit" title="Edit" data-toggle="tooltip"><i class="material-icons">&#xE254;</i></a>' + 
                            '<a class="move" title="Add as a new example" data-toggle="tooltip"><i class="material-icons">add</i></a>' +
                       '</td></tr>';

                $("#synthetic tbody").append(row);
                count++;
            }
            i++;         
        });
    }
}

function getOrdinalNum(i) {
    if(i == 1) {
        return "1st";
    } else if (i == 2) {
        return "2nd";
    } else if (i == 3) {
        return "3rd";
    } else {
        return i + "th";
    }
}

var generalizations = {};

function selectCharFamily() {
    if(typeof generalized_chars != 'undefined' && generalized_chars != null) {
        // get user selection
        $('div#charFamilies input').each(function() {
            var checkbox = $(this);
            if(checkbox.is(':checked')) {
                var id = checkbox.attr('id');
                generalizations[generalized_chars] = id;
                // uncheck the box otherwise it will still be checked the next time users open it
                checkbox.prop( "checked", false );
            }
        });
    }

    // dismiss the modal
    $('#charFamilyModal').modal('hide');

    // reset 
    generalized_chars = null;
}

function cancelCharFamily() {
    if(typeof generalized_chars != 'undefined' && generalized_chars != null) {
        // undo the highlight
        $('span.char-family').each(function() {
            var text = escapeHtml($(this).text());
            if(text == generalized_chars) {
                $(this).replaceWith($(this).text());
            }
        });
    } 

    // dismiss the modal
    $('#charFamilyModal').modal('hide');

    // reset 
    generalized_chars = null;
}

/////// New code for white box synthesis
var chart;
var chartLine;

function initLineChart(num) {
    if(chart != null) {
        chart.destroy();
        chart = null;
    }

    if(chartLine != null) {
        chartLine.destroy();
        chartLine = null;
    }

    logs = [];

    var options = { 
        series: [{
            name: "Num of Satisifed Examples",
            data: []
        }],
        chart: {
            id: "chart2",
            height: 200,
            type: 'line',
            toolbar: {
                autoSelected: 'pan',
                show: false
            }
        },
        dataLabels: {
            enabled: false
        },
        stroke: {
            curve: 'straight',
            width: 1.5
        },
        title: {
            text: 'Num of satisifed examples over the synthesis process',
            align: 'left'
        },
        grid: {
            row: {
                colors: ['#f3f3f3', 'transparent'], // takes an array which will be repeated on columns
                opacity: 0.5
            },
        },
        yaxis: {
            min: 0,
            max: num,
            tickAmount: num,
            labels: {
                formatter: function(val) {
                    return val.toFixed(0);
                }
            }
        }, 
        xaxis: {
            type: 'numeric',
            labels: {
                formatter: function(val) {
                    return val.toFixed(0);
                }
            }
        }, 
        tooltip: {
            enabled: true,
            x: {
                show: false
            }, 
            y: {
                formatter: function(val, obj) {
                    return escapeHtml(logs[obj.dataPointIndex].regex)
                },
                title: {
                    formatter: function (seriesName) {
                        return ''
                    }
                }
            }
        }
    };

    chart = new ApexCharts(document.querySelector("#chart-line2"), options);
    chart.render();
}

function updateData(arr) {
    // get the number of satisfied examples only
    var num_only = [];
    for(var i = 0; i < arr.length; i++) {
        num_only.push(arr[i].example_num);
    }


    chart.appendData([{
    // chart.updateSeries([{
        data: num_only
    }]);

    var min = 1;
    if(logs.length > 100) {
        min = logs.length - 100;

        // init the chartLine with existing data before appending the current arr
        var existing_num = [];
        for(var i = 0; i < logs.length - arr.length ; i++) {
            existing_num.push(logs[i].example_num);
        }

        if (chartLine == null) {
            // init the brush chart
            var optionsLine = {
                series: [{
                    data: existing_num
                }],
                chart: {
                    id: 'chart1',
                    height: 100,
                    type: 'area',
                    brush: {
                        target: 'chart2',
                        enabled: true
                    },
                    selection: {
                        enabled: true,
                        xaxis: {
                            min: min,
                            max: logs.length
                        }
                    },
                },
                colors: ['#008FFB'],
                fill: {
                    type: 'gradient',
                    gradient: {
                        opacityFrom: 0.91,
                        opacityTo: 0.1,
                    }
                },
                xaxis: {
                    type: 'numeric',
                    tooltip: {
                        enabled: false
                    },
                    labels: {
                        formatter: function(val) {
                            return val.toFixed(0);
                        }
                    }
                },
                yaxis: {
                    min: 0,
                    max: example_count,  
                    tickAmount: 1,
                    labels: {
                        formatter: function(val) {
                            return val.toFixed(0);
                        }
                    }
                }
            };

            chartLine = new ApexCharts(document.querySelector("#chart-line1"), optionsLine);
            chartLine.render();
        }
    }

    if(chartLine != null) {
        chartLine.updateOptions({
            chart: {
                id: 'chart1',
                height: 150,
                type: 'area',
                brush: {
                    target: 'chart2',
                    enabled: true
                },
                selection: {
                    enabled: true,
                    xaxis: {
                        min: min,
                        max: logs.length
                    }
                },
            }
        });

        chartLine.appendData([{
            data: num_only
        }]);
    }
}

var summaryData = {
    "examples": [
        {
            "input": "abcdefg",
            "positive": false,
            "stat": 2
        },
        {
            "input": ">?&*(",
            "positive": false,
            "stat": 1
        },
        {
            "input": "++",
            "positive": false,
            "stat": 5
        },
        {
            "input": "1234567890",
            "positive": true,
            "stat": 3
        },
        {
            "input": "+12+345+78+",
            "positive": true,
            "stat": 1
        },
        {
            "input": "+",
            "positive": true,
            "stat": 2
        }
    ],
    "programs": [
        {
            "program": "contain(<4>)",
            "satisfies": [
                3,
                4,
                2,
                0,
                1
            ]
        },
        {
            "program": "or(<+>,repeatrange(<3>,2,5))",
            "satisfies": [
                5,
                2,
                0,
                1
            ]
        }
    ],
    "total_programs": 14,
    "sampler_type": 'Semantic'
}

var sample_data_cache = {'MaxExamples': null, 'Unique': null, 'Semantic': null}

var SamplerTypes = ['MaxExamples', 'Unique', 'Semantic'];

var Sampler = {
    'MaxExamples': {
        'string': 'Max Examples',
        'info': 'A sample of regexes that satisfy the most number of examples'
    }, 'Unique': {
        'string': 'Unique',
        'info': 'A sample of regexes that look very different from each other'
    }, 
    'Semantic': {
        'string': 'Semantic',
        'info': 'A minimal set of regexes that are partially correct but satisfy all examples together'
    },
    'Syntax': {
        'string': 'Syntax',
        'info': 'A minimal set of regexes that cover all regex operators/constants enumerated in this iteration'
    }
}

function resetSamples(){
    $('div#programs').empty();
    sample_data_cache = {'MaxExamples': null, 'Unique': null, 'Semantic': null}
}

function showSummaryInTable(summary_data) {
    
    setTableSummaryColors(summary_data['examples'], summary_data['total_programs']);

    // Showing the programs
    $('div#programs').empty();
    var sample_div = $("<div>", {id: "samplediv", "class": "samplecard", 'style': 'width: 100%;'});
    $('div#programs').append(sample_div);
    sample_div.append(getSamplerSelector(summary_data['sampler_type']));
    var programArray = summary_data['programs'];
    var count = 0;
    var sample_programs_div = $("<div>", {id: "samplediv"});
    sample_div.append(sample_programs_div);
    programArray.forEach(function(program){
        var p_div = $("<div>", {id: "program" + count, "class": "sample form-check regex"});
        p_div.append('<input class="form-check-input" type="checkbox" value="" id="regex' + count + '">');
        p_div.append('<label class="form-check-label text-break" for="regex' + count + '">' + 
        escapeHtml(program['program']) + '</label>');
        count+=1;
        p_div.hover(function(){
            // hover in
            var satisfies = program['satisfies'];
            var tempData = [];
            for (count = 0; count < summary_data['examples'].length; count++){
                if(satisfies.includes(count)){
                    var temp_example = {
                        'input': summary_data['examples'][count]['input'],
                        'positive': summary_data['examples'][count]['positive'],
                        'stat': 1
                    }
                    tempData.push(temp_example);
                } else {
                    var temp_example = {
                        'input': summary_data['examples'][count]['input'],
                        'positive': summary_data['examples'][count]['positive'],
                        'stat': 0
                    }
                    tempData.push(temp_example);
                }
            }
            
            setTableSummaryColors(tempData, 1, 'rgba(37,204,37,1)', 'rgba(214,91,77,1)');

        }, function(){
            // hover out
            setTableSummaryColors(summary_data['examples'], summary_data['total_programs']);
        });
        sample_programs_div.append(p_div)
    });
}

function getSamplerSelector(default_sampler) {
    var html_string = `
    <div class="dropdown float-right" style="margin-bottom: 15px;">
        <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
            Regex Sample Type 
        </button>
        <div class="dropdown-menu">
            `;

    SamplerTypes.forEach((val, idx) => {
        var option_str = `<button class="dropdown-item" onclick="sampleSelectionChange('${val}')">${Sampler[val]['info']}</button>`;
        
        html_string = html_string.concat(option_str);
    });

    html_string = html_string.concat(`
        </div>
    </div>
    <label for="sampletype" style="font-size:large; font-weight: bold; margin-right: 10px;">${Sampler[default_sampler]['info']}:</label>`);

    return html_string;
}

function setTableSummaryColors(examples, program_count, primary_color = 'rgba(37,204,37,1)', secondary_color = 'rgba(255,255,255,1)'){
    resetTableColors();
    // Find the table stuff
    var rows = $("#examples tbody#task-" + cur_task +" tr");

    var stats = examples.map(e => e['stat']);
    var min_val = 0;
    var max_val = program_count;

    examples.forEach(ex => {
        // go through all the table rows and match
        rows.each(function() {
            var input = $(this).children('td').eq(0);
            var output = $(this).children('td').eq(1);
            var outputBool = output.text() == 'check';
            if(input.text() == ex['input'] && (ex['positive'] == outputBool)) {
                var satisfied = ex['stat'];
                input.prop('title', 'This example is satisfied by ' + satisfied + ' of ' + program_count + ' enumerated regexes during the synthesis process.');
                var percent = parseInt(((satisfied - min_val) * 100) / (max_val - min_val));
                var gradient_color = `linear-gradient(90deg, ${primary_color} 0%, ${primary_color} ${percent}%, ${secondary_color} ${percent}%, ${secondary_color} 100%)`;
                input.css('background', gradient_color);
            }
        });
    });
}

function resetTableColors(){
    var rows = $("#examples tbody#task-" + cur_task +" tr");
    rows.each(function() {
        // reset the color of each row
        var input = $(this).children('td').eq(0);
        // input.css('background-color', 'transparent');
        input.removeAttr('style');
        input.removeAttr('title');
    });
}

function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function regexTabClick(){
    socket.send("Click: Regex Tab");
}

function samplesTabClick(){
    socket.send("Click: Samples Tab");
}

function treeTabClick(){
    socket.send("Click: Tree Tab");
}

// function debug() {
//     socket.send("Debug");
// }

function sampleSelectionChange(sample_type) {
    // socket.send("summary: " + sample_type);
    socket.send("Click: Sample change: " + sample_type);
    if(sample_type in sample_data_cache && sample_data_cache[sample_type] !== null){
        showSummaryInTable(sample_data_cache[sample_type]);
    }
    else{
        alert('data not available for ' + sample_type + ". Please try again after some time.");
    }
}

var sum = 1;
var clickedNode = null;
var prioritized = [];
var avoided = [];
function BuildHorizontalTree(treeData) {
    // clean the previous tree
    $("#tree").empty();

    // reset the clickedNodeId
    clickedNode = null;

    // show the tree vis label
    $("div#tree-container label").attr("style", "font-size:large");

    // make the tree container scrollable
    $("#tree").attr('style', 'overflow: scroll !important;');

    var margin = { top: 10, right: 20, bottom: 10, left: 120 };
    var width = 1600 - margin.right - margin.left;
    var height = 600 - margin.top - margin.bottom;

    var i = 0, duration = 750;
    var diagonal = d3.svg.diagonal()
        .projection(function (d) { return [d.y, d.x]; });
    var svg = d3.select("#tree").append("svg")
        .attr("width", width + margin.right + margin.left)
        .attr("height", height + margin.top + margin.bottom)
      .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    root = treeData;
    root.x0 = 100;
    root.y0 = 0;
    d3.layout.tree().nodes(root).reverse().forEach(function(d) {
        // collapse all nodes that are not more than one depth
        d._children = d.children;
        if (d.children && d.depth > 0) {
            d.children = null;
        }
    });

    // create a tooltip for each tree node
    var tooltip = d3.select("body").append("div")   
    .attr("class", "treetooltip")             
    .style("opacity", 0);

    // update sum
    sum = treeData.number;

    update(root);

    // always set the horizontal scroll to the left
    $("#tree").scrollLeft(0);

    function update(source) {
        // Compute the new tree layout.
        var tree = d3.layout.tree()
            .size([height, width]);

        var nodes = tree.nodes(root).reverse(),
            links = tree.links(nodes);
        
        // adjust the width between nodes (or the length of links)
        nodes.forEach(function (d) { d.y = d.depth * 200; });
        
        // Declare the nodes…
        var node = svg.selectAll("g.node")
            .data(nodes, function (d) { return d.id || (d.id = ++i); });
        // previous: store the text color before mouseover
        // previousfont: store the text font before mouseover 
        var previous; 
        var previousfont;
        // Enter the nodes.
        var nodeEnter = node.enter().append("g")
            .attr("id", function (d) { return d.id; })
            .attr("class", "node")
            .attr("transform", function (d) {
                return "translate(" + source.y0 + "," + source.x0 + ")";
            }).on("click", nodeclick)
            // display and hide the tooltip when mousing over/out
            .on("mouseover", function(d) { 
                previous = d3.select(this).select('text').style('fill');
                previousfont = d3.select(this).select('text').style('font-size');

                d3.select(this).select('text')
                    .style('fill','#3d00e3')
                    .style('font-size', '20px')
                    .style('font-weight', 'bold');
                tooltip.transition()        
                    .duration(200)
                    .style("opacity", .9);      
                tooltip.text(d.number + " programs enumerated along this synthesis path.")  
                    .style("left", (d3.event.pageX) + "px")     
                    .style("top", (d3.event.pageY - 28) + "px");    
                })
            .on("mouseout", function(d) {      
                d3.select(this).select('text')
                    .style('fill', previous)
                    .style('font-size', previousfont)
                    .style('font-weight', 'normal');
                tooltip.transition()        
                    .duration(500)      
                    .style("opacity", 0);   
            })
            .on("contextmenu", function(d, i) {
                d3.event.preventDefault();

                clickedNode = d;

                // display the dropdown menu
                var top = d3.event.pageY;
                var left = d3.event.pageX;
                $("#context-menu").css({
                    display: "block",
                    top: top,
                    left: left
                }).addClass("show");
            });

        nodeEnter.append("circle")
         .attr("r", 10)
            .attr("stroke", function (d) { 
                if(prioritized.includes(d)) return "#ffc107";
                
                if(avoided.includes(d)) return "#6c757d";

                return d.children || d._children ? "steelblue" : "#00c13f"; })
            .style("fill", function (d) {
                if(prioritized.includes(d)) return "#ffc107";
                
                if(avoided.includes(d)) return "#6c757d";

                // calculate the background color
                var percent = 1 - d.number / sum;
                var bg_color = calculateColor(percent, "#2874A6", "#FFFFFF"); 
                return bg_color; });
            // .style("fill", function (d) { return d.children || d._children ? "lightsteelblue" : "#fff"; });
        //.attr("r", 10)
        //.style("fill", "#fff");
        nodeEnter.append("text")
            .attr("dy", ".31em")
            .attr("x", d => d.children ? -15 : 15)
            // .attr("x", 15)
            .attr("text-anchor", d => d.children ? "end" : "start")
            // .attr("text-anchor", "start")
            .text(function (d) { 
                var text = d.regex;
                // if (d.regex == '?') {
                //     text = 'All';
                // }
                return text;
                // if (d.number > 0) {
                //     return text + " [" + d.number + "]";
                // } else {
                //     return text;
                // } 
            })
            .style("fill-opacity", 1e-6)
            .style("fill", function (d) {
                if(prioritized.includes(d)) return "#ffc107";
                
                if(avoided.includes(d)) return "#6c757d";

                return "#212529";
            });

        // Transition nodes to their new position.
        var nodeUpdate = node.transition()
            .duration(duration)
            .attr("transform", function (d) { return "translate(" + d.y + "," + d.x + ")"; });
        nodeUpdate.select("text")
            .attr("x", d => d.children ? -15 : 15)
            .attr("text-anchor", d => d.children ? "end" : "start")
            .style("fill-opacity", 1);


        // Transition exiting nodes to the parent's new position.
        var nodeExit = node.exit().transition()
            .duration(duration)
            .attr("transform", function (d) { return "translate(" + source.y + "," + source.x + ")"; })
            .remove();
        nodeExit.select("circle")
            .attr("r", 1e-6);
        nodeExit.select("text")
            .style("fill-opacity", 1e-6);
        // Update the links…
        // Declare the links…
        var link = svg.selectAll("path.link")
            .data(links, function (d) { return d.target.id; });
        // Enter the links.
        link.enter().insert("path", "g")
            .attr("class", "link")
            .attr("d", function (d) {
                var o = { x: source.x0, y: source.y0 };
                return diagonal({ source: o, target: o });
            });
        // Transition links to their new position.
        link.transition()
            .duration(duration)
        .attr("d", diagonal);


        // Transition exiting nodes to the parent's new position.
        link.exit().transition()
            .duration(duration)
            .attr("d", function (d) {
                var o = { x: source.x, y: source.y };
                return diagonal({ source: o, target: o });
            })
            .remove();

        // Stash the old positions for transition.
        nodes.forEach(function (d) {
            d.x0 = d.x;
            d.y0 = d.y;
        });
    }

    // Toggle children on click.
    function nodeclick(d) {
        if (d.children) {
            d._children = d.children;
            d.children = null;
        } else {
            d.children = d._children;
            d._children = null;
        }
        update(d);
    }
}

function calculateColor(percent, min_color, max_color) {
    var min_rgb = hexToRgb(min_color);
    var max_rgb = hexToRgb(max_color);

    var resultRed = Math.round(min_rgb.red + percent * (max_rgb.red - min_rgb.red));
    var resultGreen = Math.round(min_rgb.green + percent * (max_rgb.green - min_rgb.green));
    var resultBlue = Math.round(min_rgb.blue + percent * (max_rgb.blue - min_rgb.blue));

    var rgb = { 'r': resultRed, 'g': resultGreen, 'b': resultBlue };

    return rgbToHex(resultRed, resultGreen, resultBlue);
}

function componentToHex(c) {
    var hex = c.toString(16);
    return hex.length == 1 ? "0" + hex : hex;
}

function rgbToHex(r, g, b) {
    return "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
}

function hexToRgb(hex) {
    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
        red: parseInt(result[1], 16),
        green: parseInt(result[2], 16),
        blue: parseInt(result[3], 16)
    } : null;
}

function display() {
    socket.send("Summary: " + continuous);
    displayRegex(regex_array_copy);
    $('#promptModal').modal('hide');
}

function continueSearch() {
    // simply call synthesize to continue search for another 20 seconds
    synthesize(false);
    $('#promptModal').modal('hide');
}
