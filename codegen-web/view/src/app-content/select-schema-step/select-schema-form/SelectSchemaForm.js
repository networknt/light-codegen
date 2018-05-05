/**
 * Created by Nicholas Azar on 2017-07-03.
 */

import React, {Component} from 'react';
import {Form, Upload, Icon, Select} from 'antd';
import {AppActions} from "../../../AppActions";
import {AppServices} from '../../../AppServices';

const FormItem = Form.Item;
const {Option, OptGroup} = Select;

class SelectSchemaForm extends Component {

    constructor(props) {
        super(props);
        this.state = {
            fileList: props.initValues.schemaFiles || [],
            frameworkList: []
        };
    }

    componentWillMount() {
        AppServices.get_frameworks().then((response) => {
            this.setState({frameworkList: response.data})
        })
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

    onFileRemove = (fileEvent) => {
        // since we limit the file list to one item anyway.
        this.setState({fileList: null});
        const {setFields} = this.props.form;
        setFields({
            schemaFiles: null
        });
    };

    render() {
        const { getFieldDecorator } = this.props.form;
        return (
            <Form layout="vertical" >
                <FormItem label="Framework:">
                    {getFieldDecorator('framework', {
                        initialValue: this.props.initValues.framework,
                        rules: [
                            { required: true, message: 'Please select a framework!' },
                        ],
                    })(
                    <Select placeholder="Select framework...">
                        <OptGroup label="Server Side">
                            {this.state.frameworkList ? this.state.frameworkList.map((framework) => {
                                return <Option key={framework} value={framework}>{framework}</Option>
                            }) : null}
                        </OptGroup>
                    </Select>
                    )}
                </FormItem>
                <FormItem label="Schema:">
                    <div className="dropbox">
                        {getFieldDecorator('schemaFiles', {
                            initialValue: this.props.initValues.schemaFiles,
                            getValueFromEvent: this.normFile,
                            rules: [{
                                    required: true, message: 'Please select a schema file!'
                                }
                            ]
                        })(
                        <Upload.Dragger {...AppActions.VALIDATE_JSON_UPLOAD_REQUEST} onChange={this.onFileChange} onRemove={this.onFileRemove} fileList={this.state.fileList} defaultFileList={this.props.initValues.schemaFiles}>
                            <p className="ant-upload-drag-icon">
                                <Icon type="inbox"/>
                            </p>
                            <p className="ant-upload-text">Click or drag the file to this area to upload</p>
                            <p className="ant-upload-hint">The API configuration must be in JSON format.</p>
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
        console.log("Changed fields", changedFields)
        props.onChange(changedFields);
    }
})(SelectSchemaForm);