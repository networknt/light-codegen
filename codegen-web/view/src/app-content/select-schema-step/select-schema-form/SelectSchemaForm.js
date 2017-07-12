/**
 * Created by Nicholas Azar on 2017-07-03.
 */

import React, {Component} from 'react';
import {Form, Upload, Icon, Select} from 'antd';
import {AppActions} from "../../../AppActions";

const FormItem = Form.Item;
const {Option, OptGroup} = Select;

class SelectSchemaForm extends Component {

    normFile = (e) => {
        if (Array.isArray(e)) {
            return e;
        }
        return e && e.fileList;
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
                        <Upload.Dragger {...AppActions.validateUploadedSchemaRequest} >
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
        console.log('map props', props);
        return {
            generator: {
                value: props.initValues.generator
            },
            schema: {
                value: props.initValues.schema
            }
        };
    }
})(SelectSchemaForm);