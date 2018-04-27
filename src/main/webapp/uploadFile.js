
var scriptUploadFileType = 'data';
$(function() {

    setTimeout(function() {
        var uploader = null,
        uploader = WebUploader.create({
            // 文件接收服务端。
            server:  "../MessageAction/uploadFile.do",
            timeout: 0,
            pick: {
                id: "#file-helper",
                button: '<div style="width: 100%; height: 100%;"></div>',
                style: '',
                multiple: true
            },
            threads: 2,
            fileNumLimit: 2,
            duplicate: false,
            auto: true
        });
        uploader.on("beforeFileQueued", function(file) {
            var fileName = file.name;
            if (fileName.substring(fileName.lastIndexOf(".")) != ".png" && fileName.substring(fileName.lastIndexOf(".")) != ".jpg") {
                alert('请选择png或jpg文件');
                return false;
            }
        });

        // 当一批文件添加进队列以后触发。
        uploader.on("filesQueued", function(file) {
            //debug(file.length);
        });

        // 当有文件添加进来的时候
        uploader.on("fileQueued", function(file) {
            // var typeId = file.source._refer.attr("typeId");


        });

        // 当文件被移除队列后触发
        uploader.on("fileDequeued", function(file) {
            // debug("file " + file.name + " is removed.");
        });



        uploader.on("uploadSuccess", function(file) {
            uploader.cancelFile(file);

        });

        uploader.on("uploadAccept", function(obj, response, fn) {
            if (response.error != undefined) {
                if (response.error) { //只有失败的时候才将信息往下传
                    fn(response);
                }
            }
        });

        uploader.on("uploadError", function(file, reason) {
            alert("Accept未知错误");
            return;

        });

        uploader.on("uploadComplete", function(file) {
            //$( '#'+file.id ).find('.progress').fadeOut();
            // fileApp.loadFile();
            // console.log(file);
            app.loadTempImg();
        });

        uploader.on("all", function(type) {
            //
        });

        uploader.on("error", function(type) {

        });
    }, 100);


});
