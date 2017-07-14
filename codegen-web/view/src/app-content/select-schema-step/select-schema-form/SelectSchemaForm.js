/**
 * Created by Nicholas Azar on 2017-07-03.
 */

import React, {Component} from 'react';
import {Form, Upload, Icon, Select} from 'antd';
import {AppActions} from "../../../AppActions";

const FormItem = Form.Item;
const {Option, OptGroup} = Select;

class SelectSchemaForm extends Component {

    constructor(props) {
        super(props);
        this.state = {
            fileList: props.initValues.schema || []
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
     * Doing this since I don't think the Upload component has a file limit to it..
     *
     * @param fileListObj
     */
    onFileChange = (fileListObj) => {
        this.setState({
            fileList: fileListObj.fileList.length > 0 ? [fileListObj.fileList[fileListObj.fileList.length - 1]] : []
        })
    };

    render() {
        const { getFieldDecorator } = this.props.form;
        return (
            <Form layout="vertical" >
                <FormItem label="Generator:">
                    {getFieldDecorator('generator', {
                        rules: [
                            { required: true, message: 'Please select a generator!' },
                        ],
                    })(
                    <Select placeholder="Select generator...">
                        <OptGroup label="Server Side">
                            <Option value="light-4j-rest">Light-4J REST</Option>
                            <Option value="light-4j-graphql">Light-4J GraphQL</Option>
                            <Option value="light-4j-hybrid-service">Light-4J Hybrid Service</Option>
                            <Option value="light-4j-hybrid-server">Light-4J Hybrid Server</Option>
                        </OptGroup>
                        <OptGroup label="Client Side">
                            <Option value="angular">Angular</Option>
                            <Option value="react">React</Option>
                        </OptGroup>
                    </Select>
                    )}
                </FormItem>
                <FormItem label="Schema:">
                    <div className="dropbox">
                        {getFieldDecorator('schema', {
                            valuePropName: 'schema',
                            getValueFromEvent: this.normFile,
                            rules: [{
                                    required: true, message: 'Please select a schema file!'
                                }
                            ]
                        })(
                        <Upload.Dragger {...AppActions.validateUploadedSchemaRequest} onChange={this.onFileChange} fileList={this.state.fileList} defaultFileList={this.props.initValues.schema}>
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
        props.onChange(changedFields);
    },
    mapPropsToFields(props) {
        return {
            generator: {
                value: props.initValues.generator
            }
        };
    }
})(SelectSchemaForm);