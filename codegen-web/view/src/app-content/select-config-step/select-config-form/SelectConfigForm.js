/**
 * Created by Nicholas Azar on 2017-07-05.
 */

import React, {Component} from 'react';
import {Form, Upload, Icon} from 'antd';
import {AppActions} from "../../../AppActions";

const FormItem = Form.Item;

class SelectConfigForm extends Component {

    constructor(props) {
        super(props);
        this.state = {
            fileList: props.initValues.configFiles || []
        }
    }

    normFile = (e) => {
        if (Array.isArray(e)) {
            return e;
        }
        return e && e.fileList;
    };

    /**
     * Whenever we select a new file, make sure the file list can only have 1 item.
     * Doing this since I don't think the Upload component has a file limit to it.
     * We also need to map the file name to the outer object because that's what antd wants.
     *
     * @param fileEvent
     */
    onFileChange = (fileEvent) => {
        console.log("fileEvent: ", fileEvent);
        if (fileEvent.fileList.length > 0) {
            const fileList = fileEvent.fileList.map(fileItem => {
                fileItem.name = fileItem.originFileObj.name;
                return fileItem;
            });
            this.setState({fileList: [fileList[fileList.length - 1]]})
        } else {
            this.setState({fileList: null})
        }
    };

    onFileRemove = () => {
        // since we limit the file list to one item anyway.
        this.setState({fileList: null});
        const {setFields} = this.props.form;
        setFields({
            schemaFiles: null
        });
    };

    render() {
        const {getFieldDecorator} = this.props.form;

        return (
            <Form layout="vertical">
                <FormItem label="Config:">
                    <div className="dropbox">
                        {getFieldDecorator('configFiles', {
                            initialValue: this.props.initValues.configFiles,
                            getValueFromEvent: this.normFile,
                            rules: [{
                                required: true, message: 'Please select a config file!'
                            }]
                        })(
                            <Upload.Dragger {...AppActions.VALIDATE_JSON_UPLOAD_REQUEST}
                                            onChange={this.onFileChange}
                                            onRemove={this.onFileRemove}
                                            fileList={this.state.fileList}
                                            defaultFileList={this.props.initValues.configFiles}>
                                <p className="ant-upload-drag-icon">
                                    <Icon type="inbox"/>
                                </p>
                                <p className="ant-upload-text">Click or drag the file to this area to upload</p>
                                <p className="ant-upload-hint">The config must be in JSON format.</p>
                            </Upload.Dragger>
                        )}
                    </div>
                </FormItem>
            </Form>
        )
    }
}

export default Form.create({
        onValuesChange(props, changedFields) {
            props.onChange(changedFields);
        }
    }
)(SelectConfigForm);