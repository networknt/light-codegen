/**
 * Created by Nicholas Azar on 2017-07-03.
 */

import React, {Component} from 'react';
import {Form, Upload, Icon, Select} from 'antd';

const FormItem = Form.Item;
const {Option, OptGroup} = Select;

class SelectSchemaForm extends Component {

    normFile = (e) => {
        console.log('Upload event:', e);
        if (Array.isArray(e)) {
            return e;
        }
        return e && e.fileList;
    };

    handleChange = (value) => {
        console.log(`selected ${value}`);
    };

    render() {
        const {getFieldDecorator} = this.props.form;

        return (
            <Form layout="vertical">
                <FormItem label="Generator:">
                    <Select onChange={this.handleChange} placeholder="Select generator...">
                        <OptGroup label="Client Side">
                            <Option value="angular">Angular</Option>
                            <Option value="react">React</Option>
                        </OptGroup>
                        <OptGroup label="Server Side">
                            <Option value="light-4j-graphql">Light-4J GraphQL</Option>
                        </OptGroup>
                    </Select>
                </FormItem>
                <FormItem label="Schema:">
                    <div className="dropbox">
                        {getFieldDecorator('dragger', {
                            valuePropName: 'fileList',
                            getValueFromEvent: this.normFile,
                        })(
                            <Upload.Dragger name="files" action="/upload.do">
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

export default Form.create()(SelectSchemaForm);